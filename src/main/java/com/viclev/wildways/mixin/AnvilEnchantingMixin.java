package com.viclev.wildways.mixin;

import com.viclev.wildways.EnchantingRules;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
public abstract class AnvilEnchantingMixin extends ItemCombinerMenu {
	@Shadow @Final private DataSlot cost;
	@Shadow private int repairItemCountCost;
	@Shadow private String itemName;
	@Shadow private boolean onlyRenaming;

	protected AnvilEnchantingMixin(MenuType<?> type, int id, Inventory inventory, ContainerLevelAccess access, ItemCombinerMenuSlotDefinition slots) {
		super(type, id, inventory, access, slots);
	}

	@Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
	private void wildways$recipeBooksAndMaterialRepair(CallbackInfo ci) {
		ItemStack input = this.inputSlots.getItem(0);
		ItemStack addition = this.inputSlots.getItem(1);
		if (input.is(Items.ENCHANTED_BOOK) || addition.has(DataComponents.STORED_ENCHANTMENTS)) {
			this.wildways$clearResult();
			ci.cancel();
			return;
		}
		if (!input.isEmpty() && input.isDamageableItem() && !addition.isEmpty()
			&& (input.isValidRepairItem(addition) || wildways$netheriteScrapRepairs(input, addition))) {
			if (!input.isDamaged() || !EnchantingRules.hasMending(input)) {
				this.wildways$clearResult();
			} else {
				ItemStack output = input.copy();
				output.setDamageValue(0);
				if (!StringUtil.isBlank(this.itemName) && !this.itemName.equals(input.getHoverName().getString())) {
					output.set(DataComponents.CUSTOM_NAME, Component.literal(this.itemName));
				} else if (StringUtil.isBlank(this.itemName) && input.has(DataComponents.CUSTOM_NAME)) {
					output.remove(DataComponents.CUSTOM_NAME);
				}
				this.onlyRenaming = false;
				this.repairItemCountCost = 1;
				this.cost.set(EnchantingRules.repairCost(input));
				this.resultSlots.setItem(0, output);
				this.broadcastChanges();
			}
			ci.cancel();
		}
	}

	@Unique
	private static boolean wildways$netheriteScrapRepairs(ItemStack input, ItemStack addition) {
		return addition.is(Items.NETHERITE_SCRAP) && (input.is(Items.NETHERITE_SWORD)
			|| input.is(Items.NETHERITE_PICKAXE) || input.is(Items.NETHERITE_AXE)
			|| input.is(Items.NETHERITE_SHOVEL) || input.is(Items.NETHERITE_HOE)
			|| input.is(Items.NETHERITE_HELMET) || input.is(Items.NETHERITE_CHESTPLATE)
			|| input.is(Items.NETHERITE_LEGGINGS) || input.is(Items.NETHERITE_BOOTS));
	}

	@Inject(method = "createResult", at = @At("TAIL"))
	private void wildways$freePureRename(CallbackInfo ci) {
		if (this.resultSlots.getItem(0).isEmpty()) return;
		if (this.onlyRenaming) {
			this.cost.set(0);
			return;
		}
		ItemStack input = this.inputSlots.getItem(0);
		boolean renamed = !StringUtil.isBlank(this.itemName)
			? !this.itemName.equals(input.getHoverName().getString())
			: input.has(DataComponents.CUSTOM_NAME);
		if (renamed) this.cost.set(Math.max(0, this.cost.get() - 1));
	}

	@Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
	private void wildways$allowFreeRename(Player player, boolean hasItem, CallbackInfoReturnable<Boolean> cir) {
		if (this.onlyRenaming) cir.setReturnValue(hasItem);
	}

	@Unique
	private void wildways$clearResult() {
		this.resultSlots.setItem(0, ItemStack.EMPTY);
		this.cost.set(0);
		this.repairItemCountCost = 0;
		this.onlyRenaming = false;
	}

	// This invocation is the final "too expensive" check, after compatibility checks.
	@Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;hasInfiniteMaterials()Z", ordinal = 1))
	private boolean wildways$removeFortyLevelLimit(Player player) {
		return true;
	}

	@Inject(method = "calculateIncreasedRepairCost", at = @At("HEAD"), cancellable = true)
	private static void wildways$linearPriorWorkCost(int previous, CallbackInfoReturnable<Integer> cir) {
		cir.setReturnValue((int)Math.min(previous + 1L, Integer.MAX_VALUE));
	}
}
