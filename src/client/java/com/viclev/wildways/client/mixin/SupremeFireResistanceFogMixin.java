package com.viclev.wildways.client.mixin;

import com.viclev.wildways.ModEffects;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.LavaFogEnvironment;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Retains the clear, short-range lava vision of vanilla Fire Resistance. */
@Mixin(LavaFogEnvironment.class)
public class SupremeFireResistanceFogMixin {
	@Inject(method = "setupFog", at = @At("TAIL"))
	private void wildways$applySupremeFireResistanceFog(FogData fog, Camera camera, ClientLevel level, float renderDistance, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (camera.entity() instanceof LivingEntity entity && entity.hasEffect(ModEffects.SUPREME_FIRE_RESISTANCE)) {
			fog.environmentalStart = 0.0F;
			fog.environmentalEnd = 5.0F;
		}
	}
}
