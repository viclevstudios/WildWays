package com.viclev.wildways;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

/**
 * A small server-authoritative recipe menu. The result is recalculated from the
 * three inputs and only consumes ingredients when a player actually takes it.
 */
public class FletchingTableMenu extends AbstractContainerMenu {
	private static final int LEFT = 0;
	private static final int MIDDLE = 1;
	private static final int RIGHT = 2;
	private static final int RESULT = 3;
	private static final int INPUT_END = 3;
	private static final int INVENTORY_START = 4;
	private static final int INVENTORY_END = INVENTORY_START + Inventory.INVENTORY_SIZE;

	private final ContainerLevelAccess access;
	private final Player player;
	private final InputContainer input;
	private final ResultContainer result = new ResultContainer();

	public FletchingTableMenu(int containerId, Inventory inventory) {
		this(containerId, inventory, ContainerLevelAccess.NULL);
	}

	public FletchingTableMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
		super(ModMenuTypes.FLETCHING_TABLE, containerId);
		this.access = access;
		this.player = inventory.player;
		this.input = new InputContainer(this::slotsChanged);

		this.addSlot(new InputSlot(this.input, LEFT, 26, 35));
		this.addSlot(new InputSlot(this.input, MIDDLE, 44, 35));
		this.addSlot(new InputSlot(this.input, RIGHT, 62, 35));
		this.addSlot(new ResultSlot(this.result, 0, 116, 35));
		this.addStandardInventorySlots(inventory, 8, 84);
	}

	@Override
	public void slotsChanged(Container container) {
		if (container == this.input) {
			Recipe recipe = this.findRecipe();
			ItemStack output = recipe == null ? ItemStack.EMPTY : recipe.output();
			this.result.setItem(0, output);
			this.sendResultUpdate(output);
		}
	}

	private void sendResultUpdate(ItemStack output) {
		if (this.player instanceof ServerPlayer serverPlayer) {
			this.setRemoteSlot(RESULT, output);
			serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(
				this.containerId,
				this.incrementStateId(),
				RESULT,
				output
			));
		}
	}

	private Recipe findRecipe() {
		ItemStack left = this.input.getItem(LEFT);
		ItemStack middle = this.input.getItem(MIDDLE);
		ItemStack right = this.input.getItem(RIGHT);

		if (left.is(Items.FEATHER) && middle.is(Items.STICK) && right.is(Items.FLINT)) {
			return new Recipe(new ItemStack(Items.ARROW, 4), 1, 1, 1);
		}
		if (!left.isEmpty()) {
			return null;
		}
		if (middle.is(Items.ARROW) && right.is(Items.GLOWSTONE_DUST) && right.getCount() >= 4) {
			return new Recipe(new ItemStack(Items.SPECTRAL_ARROW, 2), 0, 1, 4);
		}

		if (middle.is(Items.ARROW) && isPotion(right)) {
			int arrows = Math.min(middle.getCount(), right.is(Items.LINGERING_POTION) ? 64 : 8);
			ItemStack output = new ItemStack(Items.TIPPED_ARROW, arrows);
			output.set(DataComponents.POTION_CONTENTS, right.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY));
			return new Recipe(output, 0, arrows, 1);
		}
		if (middle.is(Items.ARROW) && right.is(Items.TURTLE_SCUTE)) {
			return new Recipe(new ItemStack(ModItems.TURTLE_ARROW, Math.min(middle.getCount(), 16)), 0, Math.min(middle.getCount(), 16), 1);
		}
		if (middle.is(Items.ARROW) && right.is(Items.PHANTOM_MEMBRANE)) {
			return new Recipe(new ItemStack(ModItems.RANGE_ARROW, Math.min(middle.getCount(), 8)), 0, Math.min(middle.getCount(), 8), 1);
		}
		if (middle.is(Items.ARROW) && right.is(Items.TNT)) {
			return new Recipe(new ItemStack(ModItems.EXPLOSIVE_ARROW, Math.min(middle.getCount(), 4)), 0, Math.min(middle.getCount(), 4), 1);
		}
		return null;
	}

	private static boolean isPotion(ItemStack stack) {
		return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
	}

	private void consumeRecipe() {
		Recipe recipe = this.findRecipe();
		if (recipe == null) {
			return;
		}
		// Do not recalculate after each individual ingredient. That transiently
		// clears the result slot and prevents vanilla-style rapid crafting.
		this.input.consume(LEFT, recipe.leftCost());
		this.input.consume(MIDDLE, recipe.middleCost());
		this.input.consume(RIGHT, recipe.rightCost());
		this.input.setChanged();
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		Slot slot = this.slots.get(slotIndex);
		if (!slot.hasItem()) {
			return ItemStack.EMPTY;
		}

		ItemStack stack = slot.getItem();
		ItemStack copied = stack.copy();
		if (slotIndex == RESULT) {
			if (!this.moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, true)) {
				return ItemStack.EMPTY;
			}
			slot.onQuickCraft(stack, copied);
		} else if (slotIndex < INVENTORY_START) {
			if (!this.moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, false)) {
				return ItemStack.EMPTY;
			}
		} else if (!this.moveItemStackTo(stack, LEFT, INPUT_END, false)) {
			return ItemStack.EMPTY;
		}

		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		slot.onTake(player, copied);
		return copied;
	}

	@Override
	public boolean stillValid(Player player) {
		return this.access.evaluate((level, pos) -> level.getBlockState(pos).is(Blocks.FLETCHING_TABLE), true);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.clearContainer(player, this.input);
	}

	private record Recipe(ItemStack output, int leftCost, int middleCost, int rightCost) {
	}

	private class ResultSlot extends Slot {
		private ResultSlot(Container container, int index, int x, int y) {
			super(container, index, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return false;
		}

		@Override
		public void onTake(Player player, ItemStack stack) {
			super.onTake(player, stack);
			FletchingTableMenu.this.consumeRecipe();
		}
	}

	private class InputSlot extends Slot {
		private InputSlot(Container container, int index, int x, int y) {
			super(container, index, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return switch (this.getContainerSlot()) {
				case LEFT -> stack.is(Items.FEATHER);
				case MIDDLE -> stack.is(Items.STICK) || stack.is(Items.ARROW);
				case RIGHT -> stack.is(Items.FLINT)
					|| stack.is(Items.GLOWSTONE_DUST)
					|| isPotion(stack)
					|| stack.is(Items.TURTLE_SCUTE)
					|| stack.is(Items.PHANTOM_MEMBRANE)
					|| stack.is(Items.TNT);
				default -> false;
			};
		}
	}

	private static class InputContainer extends SimpleContainer {
		private final Consumer<Container> changed;

		private InputContainer(Consumer<Container> changed) {
			super(3);
			this.changed = changed;
		}

		@Override
		public void setChanged() {
			super.setChanged();
			this.changed.accept(this);
		}

		private void consume(int slot, int amount) {
			this.items.get(slot).shrink(amount);
		}
	}
}
