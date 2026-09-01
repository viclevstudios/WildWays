package com.viclev.wildways.client.mixin;

import com.viclev.wildways.ModEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Lets the custom effect drive vanilla night-vision brightness without a second effect. */
@Mixin(GameRenderer.class)
public class SupremeNightVisionMixin {
	@Inject(method = "nightVisionScale", at = @At("HEAD"), cancellable = true)
	private static void wildways$supplySupremeNightVisionScale(LivingEntity entity, float partialTick, CallbackInfoReturnable<Float> cir) {
		if (entity.hasEffect(ModEffects.SUPREME_NIGHT_VISION)) {
			cir.setReturnValue(1.0F);
		}
	}
}
