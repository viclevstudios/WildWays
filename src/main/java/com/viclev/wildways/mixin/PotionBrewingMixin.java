package com.viclev.wildways.mixin;

import com.viclev.wildways.EyeBrewingRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionBrewing.class)
public class PotionBrewingMixin {
	@Inject(method = "hasMix", at = @At("HEAD"), cancellable = true)
	private void wildways$recognizeEyeRecipe(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
		if (EyeBrewingRecipes.hasMix(input, ingredient)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "mix", at = @At("HEAD"), cancellable = true)
	private void wildways$brewEye(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
		if (EyeBrewingRecipes.hasMix(input, ingredient)) {
			cir.setReturnValue(EyeBrewingRecipes.mix(input, ingredient));
		}
	}
}
