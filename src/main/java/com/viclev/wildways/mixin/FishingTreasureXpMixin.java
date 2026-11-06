package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.viclev.wildways.XpProgression;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FishingHook.class)
public abstract class FishingTreasureXpMixin {
	@WrapOperation(method = "retrieve", at = @At(value = "NEW",
		target = "(Lnet/minecraft/world/level/Level;DDDI)Lnet/minecraft/world/entity/ExperienceOrb;"))
	private ExperienceOrb wildways$doubleFishingTreasureXp(Level level, double x, double y, double z, int xp,
		Operation<ExperienceOrb> original, @Local(ordinal = 1) ItemStack caught) {
		return original.call(level, x, y, z, XpProgression.isFishingTreasure(caught) ? xp * 2 : xp);
	}
}
