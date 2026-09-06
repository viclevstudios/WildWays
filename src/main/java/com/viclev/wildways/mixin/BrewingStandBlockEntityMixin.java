package com.viclev.wildways.mixin;

import com.viclev.wildways.EyeBrewingRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {
	@Inject(method = "canPlaceItem", at = @At("HEAD"), cancellable = true)
	private void wildways$allowEyeInputs(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (slot >= 0 && slot <= 2 && EyeBrewingRecipes.isInput(stack)) {
			cir.setReturnValue(((BrewingStandBlockEntity)(Object)this).getItem(slot).isEmpty());
		}
	}
}
