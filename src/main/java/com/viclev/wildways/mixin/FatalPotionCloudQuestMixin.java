package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.viclev.wildways.BrewingQuests;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AreaEffectCloud.class)
public abstract class FatalPotionCloudQuestMixin {
	@Shadow private PotionContents potionContents;

	@Shadow
	public abstract LivingEntity getOwner();

	@Inject(method = "serverTick", at = {
		@At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"),
		@At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffect;applyInstantaneousEffect(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/LivingEntity;ID)V")
	})
	private void wildways$onPotionTarget(ServerLevel level, CallbackInfo ci, @Local LivingEntity target) {
		BrewingQuests.applyPotion(this.getOwner(), target, this.potionContents);
	}
}
