package com.viclev.wildways.mixin;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderEyeItem.class)
public class EnderEyeItemMixin {
	@Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
	private void wildways$preventVanillaEyeInsertion(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
		if (context.getLevel().getBlockState(context.getClickedPos()).is(Blocks.END_PORTAL_FRAME)) {
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}
}
