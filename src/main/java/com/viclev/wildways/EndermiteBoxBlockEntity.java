package com.viclev.wildways;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.stream.IntStream;

public class EndermiteBoxBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
	public static final int CONTAINER_SIZE = 12;
	private static final int[] SLOTS = IntStream.range(0, CONTAINER_SIZE).toArray();

	private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
	private int openCount;
	private AnimationStatus animationStatus = AnimationStatus.CLOSED;
	private float progress;
	private float progressOld;

	public EndermiteBoxBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.ENDERMITE_BOX, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, EndermiteBoxBlockEntity endermiteBox) {
		endermiteBox.updateAnimation(level, pos, state);
	}

	private void updateAnimation(Level level, BlockPos pos, BlockState state) {
		this.progressOld = this.progress;
		switch (this.animationStatus) {
			case CLOSED -> this.progress = 0.0F;
			case OPENING -> {
				this.progress += 0.1F;
				if (this.progressOld == 0.0F) {
					updateNeighbors(level, pos, state);
				}
				if (this.progress >= 1.0F) {
					this.animationStatus = AnimationStatus.OPENED;
					this.progress = 1.0F;
					updateNeighbors(level, pos, state);
				}
				this.moveCollidedEntities(level, pos, state);
			}
			case CLOSING -> {
				this.progress -= 0.1F;
				if (this.progressOld == 1.0F) {
					updateNeighbors(level, pos, state);
				}
				if (this.progress <= 0.0F) {
					this.animationStatus = AnimationStatus.CLOSED;
					this.progress = 0.0F;
					updateNeighbors(level, pos, state);
				}
			}
			case OPENED -> this.progress = 1.0F;
		}
	}

	private static void updateNeighbors(Level level, BlockPos pos, BlockState state) {
		state.updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
		level.updateNeighborsAt(pos, state.getBlock());
	}

	public AABB getBoundingBox(BlockState state) {
		return Shulker.getProgressAabb(1.0F, state.getValue(EndermiteBoxBlock.FACING), this.getProgress(1.0F), new Vec3(0.5, 0.0, 0.5));
	}

	private void moveCollidedEntities(Level level, BlockPos pos, BlockState state) {
		Direction direction = state.getValue(EndermiteBoxBlock.FACING);
		AABB movementBox = Shulker.getProgressDeltaAabb(1.0F, direction, this.progressOld, this.progress, Vec3.atBottomCenterOf(pos));
		List<Entity> entities = level.getEntities(null, movementBox);
		for (Entity entity : entities) {
			if (entity.getPistonPushReaction() == net.minecraft.world.level.material.PushReaction.IGNORE) {
				continue;
			}

			entity.move(
				MoverType.SHULKER_BOX,
				new Vec3(
					(movementBox.getXsize() + 0.01) * direction.getStepX(),
					(movementBox.getYsize() + 0.01) * direction.getStepY(),
					(movementBox.getZsize() + 0.01) * direction.getStepZ()
				)
			);
		}
	}

	@Override
	public int getContainerSize() {
		return this.items.size();
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		// Mobile containers retain their contents on removal, like vanilla shulker boxes.
	}

	@Override
	public boolean triggerEvent(int type, int data) {
		if (type == 1) {
			this.openCount = data;
			if (data == 0) {
				this.animationStatus = AnimationStatus.CLOSING;
			}
			if (data == 1) {
				this.animationStatus = AnimationStatus.OPENING;
			}
			return true;
		}

		return super.triggerEvent(type, data);
	}

	@Override
	public void startOpen(ContainerUser user) {
		if (!this.remove && !user.getLivingEntity().isSpectator()) {
			if (this.openCount < 0) {
				this.openCount = 0;
			}
			this.openCount++;
			this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);
			if (this.openCount == 1) {
				this.level.gameEvent(user.getLivingEntity(), GameEvent.CONTAINER_OPEN, this.worldPosition);
				this.level.playSound(null, this.worldPosition, SoundEvents.SHULKER_BOX_OPEN, SoundSource.BLOCKS, 0.5F, this.level.getRandom().nextFloat() * 0.1F + 0.9F);
			}
		}
	}

	@Override
	public void stopOpen(ContainerUser user) {
		if (!this.remove && !user.getLivingEntity().isSpectator()) {
			this.openCount--;
			this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);
			if (this.openCount <= 0) {
				this.level.gameEvent(user.getLivingEntity(), GameEvent.CONTAINER_CLOSE, this.worldPosition);
				this.level.playSound(null, this.worldPosition, SoundEvents.SHULKER_BOX_CLOSE, SoundSource.BLOCKS, 0.5F, this.level.getRandom().nextFloat() * 0.1F + 0.9F);
			}
		}
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return SLOTS;
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
		Block block = Block.byItem(stack.getItem());
		return !(block instanceof ShulkerBoxBlock) && block != ModBlocks.ENDERMITE_BOX;
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
		return true;
	}

	public float getProgress(float tickDelta) {
		return Mth.lerp(tickDelta, this.progressOld, this.progress);
	}

	public boolean isClosed() {
		return this.animationStatus == AnimationStatus.CLOSED;
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("block.wildways.endermite_box");
	}

	@Override
	protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
		return new EndermiteBoxMenu(containerId, inventory, this);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
		if (!this.tryLoadLootTable(input)) {
			ContainerHelper.loadAllItems(input, this.items);
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (!this.trySaveLootTable(output)) {
			ContainerHelper.saveAllItems(output, this.items, false);
		}
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.items = items;
	}

	private enum AnimationStatus {
		CLOSED,
		OPENING,
		OPENED,
		CLOSING
	}
}
