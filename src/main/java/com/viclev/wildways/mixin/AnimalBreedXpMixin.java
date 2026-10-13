package com.viclev.wildways.mixin;

import com.viclev.wildways.XpProgression;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Animal.class)
public abstract class AnimalBreedXpMixin {
	@Unique private boolean wildways$tripleBreedXp;

	@Inject(method = "finalizeSpawnChildFromBreeding", at = @At("HEAD"))
	private void wildways$rewardFirstBreeding(ServerLevel level, Animal other, AgeableMob child, CallbackInfo ci) {
		this.wildways$tripleBreedXp = XpProgression.claimFirstBreed((Animal)(Object)this, other);
	}

	@ModifyArg(
		method = "finalizeSpawnChildFromBreeding",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;<init>(Lnet/minecraft/world/level/Level;DDDI)V"),
		index = 4
	)
	private int wildways$tripleFirstBreedXp(int xp) {
		return this.wildways$tripleBreedXp ? xp * 3 : xp;
	}

	@Inject(method = "finalizeSpawnChildFromBreeding", at = @At("TAIL"))
	private void wildways$clearBreedFlag(ServerLevel level, Animal other, AgeableMob child, CallbackInfo ci) {
		this.wildways$tripleBreedXp = false;
	}
}
