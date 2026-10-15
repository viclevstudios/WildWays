package com.viclev.wildways.mixin;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractHorse.class)
public abstract class PositiveHorseBreedingMixin {
	@Redirect(
		method = "setOffspringAttribute",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/equine/AbstractHorse;createOffspringAttribute(DDDDLnet/minecraft/util/RandomSource;)D")
	)
	private double wildways$makeHorseBreedingBonusPositive(double parentA, double parentB, double minimum, double maximum, RandomSource random) {
		parentA = Mth.clamp(parentA, minimum, maximum);
		parentB = Mth.clamp(parentB, minimum, maximum);
		double margin = 0.15 * (maximum - minimum);
		double range = Math.abs(parentA - parentB) + margin * 2.0;
		double average = (parentA + parentB) / 2.0;
		double positiveQuality = Math.abs((random.nextDouble() + random.nextDouble() + random.nextDouble()) / 3.0 - 0.5);
		double value = average + range * positiveQuality;
		return Mth.clamp(value, average, maximum);
	}
}
