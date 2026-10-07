package com.viclev.wildways;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Small persistent inventory shared by the enchanting table and anvil. */
public interface WorkstationInventory extends Container {
	NonNullList<ItemStack> wildways$items();

	BlockEntity wildways$blockEntity();

	default void wildways$load(ValueInput input) {
		for (int i = 0; i < this.getContainerSize(); i++) {
			this.wildways$items().set(i, ItemStack.EMPTY);
		}
		ContainerHelper.loadAllItems(input, this.wildways$items());
	}

	default void wildways$save(ValueOutput output) {
		ContainerHelper.saveAllItems(output, this.wildways$items(), false);
	}

	@Override
	default int getContainerSize() {
		return this.wildways$items().size();
	}

	@Override
	default boolean isEmpty() {
		return this.wildways$items().stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	default ItemStack getItem(int slot) {
		return this.wildways$items().get(slot);
	}

	@Override
	default ItemStack removeItem(int slot, int amount) {
		ItemStack removed = ContainerHelper.removeItem(this.wildways$items(), slot, amount);
		if (!removed.isEmpty()) this.setChanged();
		return removed;
	}

	@Override
	default ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(this.wildways$items(), slot);
	}

	@Override
	default void setItem(int slot, ItemStack stack) {
		this.wildways$items().set(slot, stack);
		this.setChanged();
	}

	@Override
	default boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this.wildways$blockEntity(), player);
	}

	@Override
	default void clearContent() {
		for (int i = 0; i < this.getContainerSize(); i++) {
			this.wildways$items().set(i, ItemStack.EMPTY);
		}
		this.setChanged();
	}

}
