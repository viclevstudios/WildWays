package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.viclev.wildways.ProgressionItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MerchantOffer.class)
public abstract class AmethystTradePriceMixin {
	@Shadow
	public abstract ItemStack getResult();

	@ModifyReturnValue(method = "getBaseCostA", at = @At("RETURN"))
	private ItemStack wildways$displayAmethystBasePrice(ItemStack original) {
		int price = this.wildways$amethystPrice(original, original.getCount());
		return price == original.getCount() ? original : original.copyWithCount(price);
	}

	@ModifyExpressionValue(method = "getModifiedCostCount", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/trading/ItemCost;count()I"))
	private int wildways$chargeAmethystBasePrice(int original, ItemCost cost) {
		return this.wildways$amethystPrice(cost.itemStack(), original);
	}

	@Unique
	private int wildways$amethystPrice(ItemStack payment, int original) {
		return payment.is(Items.EMERALD) && ProgressionItems.isAmethystGear(this.getResult().getItem())
			? Math.round(original * 0.75F)
			: original;
	}
}
