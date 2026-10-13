package com.viclev.wildways.mixin;

import com.viclev.wildways.ProgressionItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class AmethystToolSpeedMixin {
	@Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
	private void wildways$slightlySpeedUpInefficientAmethystTools(BlockState state, CallbackInfoReturnable<Float> cir) {
		ItemStack stack = (ItemStack) (Object) this;
		if (ProgressionItems.AMETHYST_TOOLS.contains(stack.getItem()) && cir.getReturnValue() <= 1.0F) {
			cir.setReturnValue(1.5F);
		}
	}
}
