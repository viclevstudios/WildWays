package com.viclev.wildways;

import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;

/** Recipe-driven enchanting; all costs are checked again before a result is taken. */
public class WildwaysEnchantingMenu extends AbstractContainerMenu {
	private static final int TARGET = 0, LAPIS = 1, BOOK = 2, RUNE = 3, CATALYST = 4, RESULT = 5;
	private static final int INVENTORY_START = 6, INVENTORY_END = 42;
	private final Player owner;
	private final ContainerLevelAccess access;
	private final DataSlot selection = DataSlot.standalone();
	private final DataSlot shelves = DataSlot.standalone();
	private final DataSlot cost = DataSlot.standalone();
	private final DataSlot status = DataSlot.standalone();
	private final ResultContainer result = new ResultContainer();
	private final SimpleContainer input = new SimpleContainer(5) {
		@Override
		public void setChanged() {
			super.setChanged();
			WildwaysEnchantingMenu.this.slotsChanged(this);
		}
	};

	public WildwaysEnchantingMenu(int id, Inventory inventory) {
		this(id, inventory, ContainerLevelAccess.NULL);
	}

	public WildwaysEnchantingMenu(int id, Inventory inventory, ContainerLevelAccess access) {
		super(ModMenuTypes.ENCHANTING, id);
		this.owner = inventory.player;
		this.access = access;
		for (int index = 0; index < 5; index++) {
			final int inputIndex = index;
			this.addSlot(new Slot(this.input, index, 8 + index * 22, 70) {
				@Override
				public boolean mayPlace(ItemStack stack) {
					return switch (inputIndex) {
						case TARGET -> !stack.is(Items.BOOK) && !stack.is(Items.ENCHANTED_BOOK);
						case LAPIS -> stack.is(Items.LAPIS_LAZULI);
						case BOOK -> stack.is(Items.ENCHANTED_BOOK);
						case RUNE -> EnchantingItems.runeTier(stack) > 0;
						case CATALYST -> stack.is(EnchantingItems.CATALYST);
						default -> false;
					};
				}

				@Override
				public int getMaxStackSize() {
					return inputIndex == TARGET || inputIndex == BOOK || inputIndex == CATALYST ? 1 : 64;
				}
			});
		}
		this.addSlot(new Slot(this.result, 0, 150, 70) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}

			@Override
			public boolean mayPickup(Player player) {
				return player.level().isClientSide()
					? WildwaysEnchantingMenu.this.status.get() == 5
					: WildwaysEnchantingMenu.this.recipe() != null;
			}

			@Override
			public void onTake(Player player, ItemStack stack) {
				WildwaysEnchantingMenu.this.consumeRecipe(player);
				super.onTake(player, stack);
			}
		});
		this.addStandardInventorySlots(inventory, 8, 118);
		this.addDataSlot(this.selection);
		this.addDataSlot(this.shelves);
		this.addDataSlot(this.cost);
		this.addDataSlot(this.status);
	}

	public List<Holder<Enchantment>> recipes() {
		return EnchantingRules.recipes(this.input.getItem(BOOK));
	}

	public Holder<Enchantment> selectedEnchantment() {
		List<Holder<Enchantment>> recipes = this.recipes();
		return recipes.isEmpty() ? null : recipes.get(Math.floorMod(this.selection.get(), recipes.size()));
	}

	public int shelfCount() { return this.shelves.get(); }
	public int experienceCost() { return this.cost.get(); }
	public int status() { return this.status.get(); }
	public int runeTier() { return EnchantingItems.runeTier(this.input.getItem(RUNE)); }
	public boolean hasCatalyst() { return this.input.getItem(CATALYST).is(EnchantingItems.CATALYST); }

	private Recipe recipe() {
		return this.access.evaluate((level, pos) -> Optional.ofNullable(this.recipeAt(level, pos)), Optional.<Recipe>empty()).orElse(null);
	}

	private Recipe recipeAt(Level level, BlockPos pos) {
		int shelfCount = 0;
		for (var offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
			if (EnchantingTableBlock.isValidBookShelf(level, pos, offset)) {
				shelfCount++;
			}
		}
		this.shelves.set(shelfCount);
		this.cost.set(0);
		this.status.set(0);
		ItemStack target = this.input.getItem(TARGET);
		Holder<Enchantment> enchantment = this.selectedEnchantment();
		int tier = this.runeTier();
		if (target.isEmpty() || enchantment == null || tier == 0 || !this.input.getItem(LAPIS).is(Items.LAPIS_LAZULI)) {
			return null;
		}
		int enchantmentLevel = EnchantingRules.enchantmentLevel(enchantment, tier);
		if (!EnchantingRules.compatible(target, enchantment, enchantmentLevel)) {
			this.status.set(1);
			return null;
		}
		if (shelfCount < EnchantingRules.requiredShelves(tier)) {
			this.status.set(2);
			return null;
		}
		ItemStack catalyst = this.input.getItem(CATALYST);
		if (!catalyst.isEmpty() && !catalyst.is(EnchantingItems.CATALYST)) {
			this.status.set(3);
			return null;
		}
		int xpCost = EnchantingRules.experienceCost(tier, !catalyst.isEmpty());
		this.cost.set(xpCost);
		if (!this.owner.hasInfiniteMaterials() && this.owner.experienceLevel < xpCost) {
			this.status.set(4);
			return null;
		}
		ItemStack output = target.copyWithCount(1);
		output.enchant(enchantment, enchantmentLevel);
		this.status.set(5);
		return new Recipe(output, xpCost);
	}

	@Override
	public void slotsChanged(Container container) {
		if (container == this.input && this.owner != null && !this.owner.level().isClientSide()) {
			this.updateResult();
		}
	}

	private void updateResult() {
		Recipe recipe = this.recipe();
		this.result.setItem(0, recipe == null ? ItemStack.EMPTY : recipe.output());
	}

	@Override
	public void broadcastChanges() {
		if (!this.owner.level().isClientSide()) {
			this.updateResult();
		}
		super.broadcastChanges();
	}

	private void consumeRecipe(Player player) {
		Recipe recipe = this.recipe();
		if (recipe == null) {
			return;
		}
		player.onEnchantmentPerformed(recipe.output(), player.hasInfiniteMaterials() ? 0 : recipe.cost());
		this.input.getItem(TARGET).shrink(1);
		this.input.getItem(LAPIS).consume(1, player);
		this.input.getItem(RUNE).consume(1, player);
		ItemStack catalyst = this.input.getItem(CATALYST);
		if (!catalyst.isEmpty() && !player.hasInfiniteMaterials()) {
			int damage = catalyst.getDamageValue() + 1;
			if (damage >= catalyst.getMaxDamage()) {
				catalyst.shrink(1);
			} else {
				catalyst.setDamageValue(damage);
			}
		}
		player.awardStat(Stats.ENCHANT_ITEM);
		if (player instanceof ServerPlayer serverPlayer) {
			CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, recipe.output(), recipe.cost());
		}
		this.access.execute((level, pos) -> level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F));
		this.input.setChanged();
	}

	@Override
	public boolean clickMenuButton(Player player, int button) {
		int size = this.recipes().size();
		if ((button != 0 && button != 1) || size < 2) {
			return false;
		}
		this.selection.set(Math.floorMod(this.selection.get() + (button == 0 ? -1 : 1), size));
		this.updateResult();
		this.broadcastChanges();
		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = this.slots.get(index);
		if (!slot.hasItem() || !slot.mayPickup(player)) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = slot.getItem();
		ItemStack copy = stack.copy();
		if (index < INVENTORY_START) {
			if (!this.moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, true)) {
				return ItemStack.EMPTY;
			}
		} else {
			int destination = stack.is(Items.LAPIS_LAZULI) ? LAPIS : stack.is(Items.ENCHANTED_BOOK) ? BOOK
				: EnchantingItems.runeTier(stack) > 0 ? RUNE : stack.is(EnchantingItems.CATALYST) ? CATALYST : TARGET;
			if (!this.moveItemStackTo(stack, destination, destination + 1, false)) {
				return ItemStack.EMPTY;
			}
		}
		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		slot.onTake(player, copy);
		return copy;
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
		return slot.container != this.result && super.canTakeItemForPickAll(stack, slot);
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(this.access, player, Blocks.ENCHANTING_TABLE);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.clearContainer(player, this.input);
	}

	private record Recipe(ItemStack output, int cost) {
	}
}
