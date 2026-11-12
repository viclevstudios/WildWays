package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.viclev.wildways.BrewingQuests;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownSplashPotion.class)
public class FatalSplashPotionQuestMixin {
	@Inject(method = "onHitAsPotion", at = {
		@At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"),
		@At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffect;applyInstantaneousEffect(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/LivingEntity;ID)V")
	})
	private void wildways$onPotionTarget(ServerLevel level, ItemStack stack, HitResult hit, CallbackInfo ci,
		@Local LivingEntity target) {
		var potion = (ThrownSplashPotion)(Object)this;
		BrewingQuests.applyPotion(potion.getOwner(), target,
			stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY));
	}
}
