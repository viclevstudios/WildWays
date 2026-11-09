package com.viclev.wildways.mixin;

import com.viclev.wildways.FishingBait;
import com.viclev.wildways.PlantQuality;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SingleItemRecipe.class)
public abstract class FishingFoodCookingMixin {
	@Inject(method = "assemble(Lnet/minecraft/world/item/crafting/SingleRecipeInput;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"))
	private void wildways$preserveFishSaturation(SingleRecipeInput input, CallbackInfoReturnable<ItemStack> cir) {
		if ((Object) this instanceof AbstractCookingRecipe) {
			if (input.item().has(FishingBait.RICH_FISH)) {
				FishingBait.enrichFish(cir.getReturnValue());
			}
			if (input.item().is(Items.POTATO) && input.item().has(PlantQuality.LEVEL_TWO) && cir.getReturnValue().is(Items.BAKED_POTATO)) {
				PlantQuality.mark(cir.getReturnValue());
			}
		}
	}
}
