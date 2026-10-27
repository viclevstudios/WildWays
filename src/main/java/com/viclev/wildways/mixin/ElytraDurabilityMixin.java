package com.viclev.wildways.mixin;

import com.viclev.wildways.ElytraDurability;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class ElytraDurabilityMixin {
	@Inject(method = "updateFallFlying", at = @At("HEAD"), cancellable = true)
	private void wildways$preserveLastElytraDurability(CallbackInfo ci) {
		LivingEntity entity = (LivingEntity)(Object)this;
		if (entity.isFallFlying() && ElytraDurability.isAtLastPoint(entity.getItemBySlot(EquipmentSlot.CHEST))) {
			entity.checkFallDistanceAccumulation();
			if (!entity.onGround()) {
				// Keep the final durability point while airborne. Vanilla then prevents
				// starting another glide after landing because the next hit would break it.
				ci.cancel();
			}
		}
	}
}
