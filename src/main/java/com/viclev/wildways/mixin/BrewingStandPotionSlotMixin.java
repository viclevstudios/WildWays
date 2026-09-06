package com.viclev.wildways.mixin;

import com.viclev.wildways.EyeBrewingRecipes;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.BrewingStandMenu$PotionSlot")
public class BrewingStandPotionSlotMixin {
	@Inject(method = "mayPlaceItem", at = @At("HEAD"), cancellable = true)
	private static void wildways$allowEyeInputs(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (EyeBrewingRecipes.isInput(stack)) {
			cir.setReturnValue(true);
		}
	}
}
