package com.viclev.wildways.mixin;

import com.viclev.wildways.FletchingTableMenu;
import com.viclev.wildways.WildwaysEnchantingMenu;
import com.viclev.wildways.XpProgression;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class WorkstationCompletionXpMixin {
	@Shadow @Final public NonNullList<Slot> slots;

	@Unique private Block wildways$pendingStation;
	@Unique private ItemStack wildways$pendingOutput = ItemStack.EMPTY;
	@Unique private int wildways$inventoryCountBefore;
	@Unique private int wildways$carriedCountBefore;
	@Unique private int wildways$pendingSlot = -1;

	@Inject(method = "clicked", at = @At("HEAD"))
	private void wildways$recordStationOutput(int slotId, int button, ContainerInput clickType, Player player, CallbackInfo ci) {
		this.wildways$clearPending();
		if (!(player instanceof ServerPlayer) || slotId < 0 || slotId >= this.slots.size()) {
			return;
		}

		AbstractContainerMenu menu = (AbstractContainerMenu)(Object)this;
		Block station = this.wildways$stationFor(menu, slotId);
		if (station == null) {
			return;
		}
		ItemStack output = this.slots.get(slotId).getItem();
		if (output.isEmpty() || (menu instanceof BrewingStandMenu && !this.wildways$isBrewedPotion(output))) {
			return;
		}

		this.wildways$pendingStation = station;
		this.wildways$pendingSlot = slotId;
		this.wildways$pendingOutput = output.copy();
		this.wildways$inventoryCountBefore = this.wildways$countMatching(player.getInventory(), output);
		this.wildways$carriedCountBefore = this.wildways$countMatching(player.containerMenu.getCarried(), output);
	}

	@Inject(method = "clicked", at = @At("TAIL"))
	private void wildways$awardAfterStationOutputTaken(int slotId, int button, ContainerInput clickType, Player player, CallbackInfo ci) {
		if (this.wildways$pendingStation != null && player instanceof ServerPlayer serverPlayer) {
			ItemStack after = slotId >= 0 && slotId < this.slots.size() ? this.slots.get(slotId).getItem() : ItemStack.EMPTY;
			boolean outputSlotChanged = after.getCount() < this.wildways$pendingOutput.getCount()
				|| (!after.isEmpty() && !ItemStack.isSameItemSameComponents(after, this.wildways$pendingOutput));
			boolean receivedOutput = this.wildways$countMatching(player.getInventory(), this.wildways$pendingOutput) > this.wildways$inventoryCountBefore
				|| this.wildways$countMatching(player.containerMenu.getCarried(), this.wildways$pendingOutput) > this.wildways$carriedCountBefore;
			if (outputSlotChanged || receivedOutput) {
				XpProgression.awardWorkstationOnce(serverPlayer, this.wildways$pendingStation);
			}
		}
		this.wildways$clearPending();
	}

	@Unique
	private Block wildways$stationFor(AbstractContainerMenu menu, int slotId) {
		if (menu instanceof WildwaysEnchantingMenu) return slotId == 5 ? Blocks.ENCHANTING_TABLE : null;
		if (menu instanceof FletchingTableMenu) return slotId == 3 ? Blocks.FLETCHING_TABLE : null;
		if (menu instanceof AnvilMenu) return slotId == AnvilMenu.RESULT_SLOT ? Blocks.ANVIL : null;
		if (menu instanceof StonecutterMenu) return slotId == StonecutterMenu.RESULT_SLOT ? Blocks.STONECUTTER : null;
		if (menu instanceof CartographyTableMenu) return slotId == CartographyTableMenu.RESULT_SLOT ? Blocks.CARTOGRAPHY_TABLE : null;
		if (menu instanceof GrindstoneMenu) return slotId == GrindstoneMenu.RESULT_SLOT ? Blocks.GRINDSTONE : null;
		if (menu instanceof SmithingMenu) return slotId == SmithingMenu.RESULT_SLOT ? Blocks.SMITHING_TABLE : null;
		if (menu instanceof LoomMenu loom) return slotId == menu.slots.indexOf(loom.getResultSlot()) ? Blocks.LOOM : null;
		if (menu instanceof BrewingStandMenu) return slotId < 3 ? Blocks.BREWING_STAND : null;
		return null;
	}

	@Unique
	private boolean wildways$isBrewedPotion(ItemStack stack) {
		if (!stack.is(Items.POTION) && !stack.is(Items.SPLASH_POTION) && !stack.is(Items.LINGERING_POTION)) {
			return false;
		}
		PotionContents contents = stack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
		return contents != null && !contents.is(Potions.WATER) && (contents.potion().isPresent() || contents.hasEffects());
	}

	@Unique
	private int wildways$countMatching(Inventory inventory, ItemStack expected) {
		int count = 0;
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack current = inventory.getItem(slot);
			if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, expected)) {
				count += current.getCount();
			}
		}
		return count;
	}

	@Unique
	private int wildways$countMatching(ItemStack current, ItemStack expected) {
		return !current.isEmpty() && ItemStack.isSameItemSameComponents(current, expected) ? current.getCount() : 0;
	}

	@Unique
	private void wildways$clearPending() {
		this.wildways$pendingStation = null;
		this.wildways$pendingOutput = ItemStack.EMPTY;
		this.wildways$pendingSlot = -1;
	}
}
