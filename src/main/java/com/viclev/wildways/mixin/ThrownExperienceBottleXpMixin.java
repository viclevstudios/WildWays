package com.viclev.wildways.mixin;

import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ThrownExperienceBottle.class)
public abstract class ThrownExperienceBottleXpMixin {
	@ModifyArg(
		method = "onHit",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/ExperienceOrb;awardWithDirection(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;I)V"
		),
		index = 3
	)
	private int wildways$doubleExperienceBottleReward(int xp) {
		return xp * 2;
	}
}
