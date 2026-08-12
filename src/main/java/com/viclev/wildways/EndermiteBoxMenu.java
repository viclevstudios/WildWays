package com.viclev.wildways;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ShulkerBoxSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class EndermiteBoxMenu extends AbstractContainerMenu {
	private static final int SLOT_COLUMNS = 4;
	private static final int SLOT_ROWS = 3;
	private static final int SLOT_COUNT = SLOT_COLUMNS * SLOT_ROWS;
	private static final int CONTAINER_END = SLOT_COUNT;
	private static final int INVENTORY_START = CONTAINER_END;
	private static final int INVENTORY_END = INVENTORY_START + Inventory.INVENTORY_SIZE;
	private static final int CONTAINER_START_X = 44;
	private static final int CONTAINER_START_Y = 18;
	private static final int INVENTORY_START_X = 8;
	private static final int INVENTORY_START_Y = 84;

	private final Container container;

	public EndermiteBoxMenu(int containerId, Inventory inventory) {
		this(containerId, inventory, new SimpleContainer(SLOT_COUNT));
	}

	public EndermiteBoxMenu(int containerId, Inventory inventory, Container container) {
		super(ModMenuTypes.ENDERMITE_BOX, containerId);
		checkContainerSize(container, SLOT_COUNT);
		this.container = container;
		container.startOpen(inventory.player);

		for (int y = 0; y < SLOT_ROWS; y++) {
			for (int x = 0; x < SLOT_COLUMNS; x++) {
				int slot = x + y * SLOT_COLUMNS;
				this.addSlot(new ShulkerBoxSlot(this.container, slot, CONTAINER_START_X + x * SLOT_SIZE, CONTAINER_START_Y + y * SLOT_SIZE));
			}
		}

		this.addStandardInventorySlots(inventory, INVENTORY_START_X, INVENTORY_START_Y);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		Slot slot = this.slots.get(slotIndex);
		if (!slot.hasItem()) {
			return ItemStack.EMPTY;
		}

		ItemStack stack = slot.getItem();
		ItemStack clicked = stack.copy();
		if (slotIndex < CONTAINER_END) {
			if (!this.moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, true)) {
				return ItemStack.EMPTY;
			}
		} else if (!this.moveItemStackTo(stack, 0, CONTAINER_END, false)) {
			return ItemStack.EMPTY;
		}

		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}

		return clicked;
	}

	@Override
	public boolean stillValid(Player player) {
		return this.container.stillValid(player);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.container.stopOpen(player);
	}
}
