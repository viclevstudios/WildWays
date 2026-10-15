package com.viclev.wildways.mixin;

import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AbstractBoat.class)
public abstract class BoatWaterSpeedMixin {
	@Shadow private AbstractBoat.Status status;

	@ModifyConstant(method = "controlBoat", constant = @org.spongepowered.asm.mixin.injection.Constant(floatValue = 0.04F))
	private float wildways$slightlyIncreaseWaterAcceleration(float vanillaAcceleration) {
		return this.status == AbstractBoat.Status.IN_WATER ? 0.05F : vanillaAcceleration;
	}
}
