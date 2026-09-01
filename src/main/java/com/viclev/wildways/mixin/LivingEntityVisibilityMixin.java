package com.viclev.wildways.mixin;

import com.viclev.wildways.ModEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Supreme invisibility is deliberately half as detectable as vanilla invisibility. */
@Mixin(LivingEntity.class)
public class LivingEntityVisibilityMixin {
	@Inject(method = "updateInvisibilityStatus", at = @At("TAIL"))
	private void wildways$applySupremeInvisibility(CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self.hasEffect(ModEffects.SUPREME_INVISIBILITY)) {
			self.setInvisible(true);
		}
	}

	@Inject(method = "getVisibilityPercent", at = @At("RETURN"), cancellable = true)
	private void wildways$reduceSupremeInvisibilityVisibility(Entity lookingEntity, CallbackInfoReturnable<Double> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self.hasEffect(ModEffects.SUPREME_INVISIBILITY)) {
			cir.setReturnValue(cir.getReturnValue() * 0.5D);
		}
	}
}
