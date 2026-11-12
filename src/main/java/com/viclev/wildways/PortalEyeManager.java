package com.viclev.wildways;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PortalEyeManager {
	private record PortalRing(BlockPos center, List<BlockPos> frames) {
	}

	private PortalEyeManager() {
	}

	public static void initialize() {
		ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> {
			if (generated) {
				return;
			}
			data(level).forEachInChunk(chunk.getPos(), (pos, stack) -> {
				BlockState state = chunk.getBlockState(pos);
				if (state.is(Blocks.END_PORTAL_FRAME)
					&& state.getValue(EndPortalFrameBlock.HAS_EYE)
					&& state.getValue(PortalEyeStates.EYE_TYPE) == 0
					&& ModItems.isPortalEye(stack.getItem())) {
					level.setBlock(pos, state.setValue(PortalEyeStates.EYE_TYPE, PortalEyeStates.typeFor(stack.getItem())), Block.UPDATE_CLIENTS);
				}
			});
		});

		AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
			BlockState state = level.getBlockState(pos);
			if (!state.is(Blocks.END_PORTAL_FRAME) || !state.getValue(EndPortalFrameBlock.HAS_EYE) || player.isSpectator()) {
				return InteractionResult.PASS;
			}

			if (!level.isClientSide()) {
				removeEye((ServerLevel)level, pos, state, player);
			}
			return InteractionResult.SUCCESS;
		});
	}

	public static InteractionResult rejectUnenchanted(UseOnContext context) {
		BlockState state = context.getLevel().getBlockState(context.getClickedPos());
		if (!state.is(Blocks.END_PORTAL_FRAME)) {
			return InteractionResult.PASS;
		}

		Player player = context.getPlayer();
		if (player != null && !context.getLevel().isClientSide()) {
			player.sendOverlayMessage(Component.translatable("message.wildways.enchanted_eye_required"));
		}
		return InteractionResult.FAIL;
	}

	public static InteractionResult tryPlace(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);
		if (!state.is(Blocks.END_PORTAL_FRAME) || state.getValue(EndPortalFrameBlock.HAS_EYE)) {
			return InteractionResult.PASS;
		}

		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		ServerLevel serverLevel = (ServerLevel)level;
		PortalEyeData data = data(serverLevel);
		ItemStack placedStack = context.getItemInHand();
		PortalRing ring = findPortalRing(serverLevel, pos, state.getValue(EndPortalFrameBlock.FACING));
		if (ring != null && ring.frames().stream().anyMatch(frame -> eyeInFrame(serverLevel, data, frame).is(placedStack.getItem()))) {
			Player player = context.getPlayer();
			if (player != null) {
				player.sendOverlayMessage(Component.translatable("message.wildways.duplicate_portal_eye"));
			}
			return InteractionResult.FAIL;
		}

		BlockState newState = state.setValue(EndPortalFrameBlock.HAS_EYE, true)
			.setValue(PortalEyeStates.EYE_TYPE, PortalEyeStates.typeFor(placedStack.getItem()));
		Block.pushEntitiesUp(state, newState, level, pos);
		level.setBlock(pos, newState, Block.UPDATE_CLIENTS);
		level.updateNeighbourForOutputSignal(pos, Blocks.END_PORTAL_FRAME);
		data.put(pos, placedStack);
		ServerPlayer player = context.getPlayer() instanceof ServerPlayer serverPlayer ? serverPlayer : null;
		if (player != null) {
			CollectorQuests.checkInventory(player);
		}
		placedStack.shrink(1);
		level.levelEvent(1503, pos, 0);
		if (ring != null) {
			tryOpenPortal(serverLevel, ring, data, player);
		}
		return InteractionResult.SUCCESS_SERVER;
	}

	private static void removeEye(ServerLevel level, BlockPos pos, BlockState state, Player player) {
		PortalEyeData data = data(level);
		ItemStack stack = data.remove(pos);
		if (stack.isEmpty()) {
			stack = new ItemStack(Items.ENDER_EYE);
		}

		level.setBlock(pos, state.setValue(EndPortalFrameBlock.HAS_EYE, false)
			.setValue(PortalEyeStates.EYE_TYPE, 0), Block.UPDATE_ALL);
		level.updateNeighbourForOutputSignal(pos, Blocks.END_PORTAL_FRAME);
		PortalRing ring = findPortalRing(level, pos, state.getValue(EndPortalFrameBlock.FACING));
		if (ring != null) {
			for (BlockPos interior : portalInterior(ring.center())) {
				if (level.getBlockState(interior).is(Blocks.END_PORTAL)) {
					level.setBlock(interior, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
				}
			}
		}
		if (!player.getInventory().add(stack)) {
			player.drop(stack, false, Prediction.SERVER_ONLY);
		}
		level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	private static PortalRing findPortalRing(ServerLevel level, BlockPos frame, Direction inward) {
		BlockPos middle = frame.relative(inward, 2);
		Direction across = inward.getClockWise();
		for (int offset = -1; offset <= 1; offset++) {
			BlockPos center = middle.relative(across, offset);
			List<BlockPos> frames = findFrames(level, center);
			if (!frames.isEmpty()) {
				return new PortalRing(center, frames);
			}
		}
		return null;
	}

	private static List<BlockPos> findFrames(ServerLevel level, BlockPos center) {
		List<BlockPos> frames = new ArrayList<>(12);
		for (Direction outward : Direction.Plane.HORIZONTAL) {
			BlockPos side = center.relative(outward, 2);
			for (int offset = -1; offset <= 1; offset++) {
				BlockPos pos = side.relative(outward.getClockWise(), offset);
				BlockState state = level.getBlockState(pos);
				if (!state.is(Blocks.END_PORTAL_FRAME) || state.getValue(EndPortalFrameBlock.FACING) != outward.getOpposite()) {
					return List.of();
				}
				frames.add(pos);
			}
		}
		return frames;
	}

	private static ItemStack eyeInFrame(ServerLevel level, PortalEyeData data, BlockPos frame) {
		BlockState state = level.getBlockState(frame);
		return state.is(Blocks.END_PORTAL_FRAME) && state.getValue(EndPortalFrameBlock.HAS_EYE) ? data.get(frame) : ItemStack.EMPTY;
	}

	private static void tryOpenPortal(ServerLevel level, PortalRing ring, PortalEyeData data, ServerPlayer player) {
		Set<Item> found = new HashSet<>();
		for (BlockPos frame : ring.frames()) {
			Item eye = eyeInFrame(level, data, frame).getItem();
			if (!ModItems.isPortalEye(eye) || !found.add(eye)) {
				return;
			}
		}

		for (BlockPos pos : portalInterior(ring.center())) {
			level.destroyBlock(pos, false);
			level.setBlock(pos, Blocks.END_PORTAL.defaultBlockState(), Block.UPDATE_CLIENTS);
		}
		level.globalLevelEvent(1038, ring.center(), 0);
		if (player != null) {
			XpProgression.awardOnce(player, "portal:minecraft:the_end", 75);
			CollectorQuests.portalOpened(player);
		}
	}

	private static Iterable<BlockPos> portalInterior(BlockPos center) {
		return BlockPos.betweenClosed(center.offset(-1, 0, -1), center.offset(1, 0, 1));
	}

	private static PortalEyeData data(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(PortalEyeData.TYPE);
	}
}
