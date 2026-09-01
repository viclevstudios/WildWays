package com.viclev.wildways.mixin;

import com.viclev.wildways.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Applies the movement mechanics directly, without adding a second vanilla effect. */
@Mixin(LivingEntity.class)
public class SupremeMovementMixin {
	@Inject(method = "getEffectiveGravity", at = @At("RETURN"), cancellable = true)
	private void wildways$applySupremeSlowFallingGravity(CallbackInfoReturnable<Double> cir) {
		LivingEntity entity = (LivingEntity) (Object) this;
		if (entity.getDeltaMovement().y <= 0.0D && entity.hasEffect(ModEffects.SUPREME_SLOW_FALLING)) {
			cir.setReturnValue(Math.min(cir.getReturnValue(), 0.01D));
		}
	}

	@Inject(method = "travelInLava", at = @At("TAIL"))
	private void wildways$swimFasterInLava(Vec3 input, double gravity, boolean falling, double verticalSpeed, CallbackInfo ci) {
		LivingEntity entity = (LivingEntity) (Object) this;
		if (entity.hasEffect(ModEffects.SUPREME_FIRE_RESISTANCE)) {
			Vec3 movement = entity.getDeltaMovement();
			entity.setDeltaMovement(movement.x * 1.50D, movement.y, movement.z * 1.50D);
		}
	}

	@Inject(method = "travelInAir", at = @At("TAIL"))
	private void wildways$moveFasterWhileSlowFalling(Vec3 input, CallbackInfo ci) {
		LivingEntity entity = (LivingEntity) (Object) this;
		if (!entity.onGround() && entity.hasEffect(ModEffects.SUPREME_SLOW_FALLING)) {
			Vec3 movement = entity.getDeltaMovement();
			entity.setDeltaMovement(movement.x * 1.04D, movement.y, movement.z * 1.04D);
		}
	}
}
