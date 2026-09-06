package com.viclev.wildways;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.state.pattern.BlockPattern;

import java.util.HashSet;
import java.util.Set;

public final class PortalEyeManager {
	private static final int FRAME_SCAN_RADIUS = 5;

	private PortalEyeManager() {
	}

	public static void initialize() {
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
		if (containsEyeType(serverLevel, data, pos, placedStack.getItem())) {
			Player player = context.getPlayer();
			if (player != null) {
				player.sendOverlayMessage(Component.translatable("message.wildways.duplicate_portal_eye"));
			}
			return InteractionResult.FAIL;
		}

		BlockState newState = state.setValue(EndPortalFrameBlock.HAS_EYE, true);
		Block.pushEntitiesUp(state, newState, level, pos);
		level.setBlock(pos, newState, Block.UPDATE_CLIENTS);
		level.updateNeighbourForOutputSignal(pos, Blocks.END_PORTAL_FRAME);
		data.put(pos, placedStack);
		placedStack.shrink(1);
		level.levelEvent(1503, pos, 0);
		tryOpenPortal(serverLevel, pos, data);
		return InteractionResult.SUCCESS_SERVER;
	}

	private static void removeEye(ServerLevel level, BlockPos pos, BlockState state, Player player) {
		PortalEyeData data = data(level);
		ItemStack stack = data.remove(pos);
		if (stack.isEmpty()) {
			stack = new ItemStack(Items.ENDER_EYE);
		}

		level.setBlock(pos, state.setValue(EndPortalFrameBlock.HAS_EYE, false), Block.UPDATE_ALL);
		level.updateNeighbourForOutputSignal(pos, Blocks.END_PORTAL_FRAME);
		closeNearbyPortal(level, pos);
		if (!player.getInventory().add(stack)) {
			player.drop(stack, false);
		}
		level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	private static boolean containsEyeType(ServerLevel level, PortalEyeData data, BlockPos center, Item type) {
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-FRAME_SCAN_RADIUS, -1, -FRAME_SCAN_RADIUS), center.offset(FRAME_SCAN_RADIUS, 1, FRAME_SCAN_RADIUS))) {
			BlockState state = level.getBlockState(pos);
			if (!state.is(Blocks.END_PORTAL_FRAME) || !state.getValue(EndPortalFrameBlock.HAS_EYE)) {
				data.remove(pos);
				continue;
			}

			if (data.get(pos).is(type)) {
				return true;
			}
		}
		return false;
	}

	private static void tryOpenPortal(ServerLevel level, BlockPos insertedAt, PortalEyeData data) {
		BlockPattern.BlockPatternMatch match = EndPortalFrameBlock.getOrCreatePortalShape().find(level, insertedAt);
		if (match == null || !hasAllUniqueEyes(level, data, insertedAt)) {
			return;
		}

		BlockPos corner = match.getFrontTopLeft().offset(-3, 0, -3);
		for (int x = 0; x < 3; x++) {
			for (int z = 0; z < 3; z++) {
				BlockPos portalPos = corner.offset(x, 0, z);
				level.destroyBlock(portalPos, false);
				level.setBlock(portalPos, Blocks.END_PORTAL.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
		}
		level.globalLevelEvent(1038, corner.offset(1, 0, 1), 0);
	}

	private static boolean hasAllUniqueEyes(ServerLevel level, PortalEyeData data, BlockPos center) {
		Set<Item> found = new HashSet<>();
		int frames = 0;
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-FRAME_SCAN_RADIUS, -1, -FRAME_SCAN_RADIUS), center.offset(FRAME_SCAN_RADIUS, 1, FRAME_SCAN_RADIUS))) {
			BlockState state = level.getBlockState(pos);
			if (!state.is(Blocks.END_PORTAL_FRAME) || !state.getValue(EndPortalFrameBlock.HAS_EYE)) {
				continue;
			}

			ItemStack stack = data.get(pos);
			if (!ModItems.isPortalEye(stack.getItem())) {
				return false;
			}
			frames++;
			found.add(stack.getItem());
		}
		return frames == ModItems.PORTAL_EYES.size() && found.containsAll(ModItems.PORTAL_EYE_SET);
	}

	private static void closeNearbyPortal(ServerLevel level, BlockPos center) {
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-4, -1, -4), center.offset(4, 1, 4))) {
			if (level.getBlockState(pos).is(Blocks.END_PORTAL)) {
				level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
		}
	}

	private static PortalEyeData data(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(PortalEyeData.TYPE);
	}
}
