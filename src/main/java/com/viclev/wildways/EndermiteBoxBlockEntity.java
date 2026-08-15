package com.viclev.wildways;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ContainerUser;
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

import java.util.stream.IntStream;

public class EndermiteBoxBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
	public static final int CONTAINER_SIZE = 12;
	private static final int[] SLOTS = IntStream.range(0, CONTAINER_SIZE).toArray();

	private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
	private int openCount;

	public EndermiteBoxBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.ENDERMITE_BOX, pos, state);
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
	public void startOpen(ContainerUser user) {
		if (!this.remove && !user.getLivingEntity().isSpectator()) {
			if (this.openCount < 0) {
				this.openCount = 0;
			}
			this.openCount++;
			if (this.openCount == 1) {
				this.setOpen(true);
				this.level.gameEvent(user.getLivingEntity(), GameEvent.CONTAINER_OPEN, this.worldPosition);
				this.level.playSound(null, this.worldPosition, SoundEvents.SHULKER_BOX_OPEN, SoundSource.BLOCKS, 0.5F, this.level.getRandom().nextFloat() * 0.1F + 0.9F);
			}
		}
	}

	@Override
	public void stopOpen(ContainerUser user) {
		if (!this.remove && !user.getLivingEntity().isSpectator()) {
			this.openCount = Math.max(0, this.openCount - 1);
			if (this.openCount <= 0) {
				this.setOpen(false);
				this.level.gameEvent(user.getLivingEntity(), GameEvent.CONTAINER_CLOSE, this.worldPosition);
				this.level.playSound(null, this.worldPosition, SoundEvents.SHULKER_BOX_CLOSE, SoundSource.BLOCKS, 0.5F, this.level.getRandom().nextFloat() * 0.1F + 0.9F);
			}
		}
	}

	private void setOpen(boolean open) {
		BlockState state = this.getBlockState();
		if (!this.level.isClientSide() && state.getValue(EndermiteBoxBlock.OPEN) != open) {
			this.level.setBlock(this.worldPosition, state.setValue(EndermiteBoxBlock.OPEN, open), Block.UPDATE_ALL);
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

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("block.wildways.endermite_nest");
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

}
