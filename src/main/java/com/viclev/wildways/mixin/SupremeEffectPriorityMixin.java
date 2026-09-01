package com.viclev.wildways.mixin;

import com.viclev.wildways.ModEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Gives each Supreme effect the same priority over its vanilla counterpart as a higher vanilla tier. */
@Mixin(LivingEntity.class)
public class SupremeEffectPriorityMixin {
	@Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
	private void wildways$prioritizeSupremeEffects(MobEffectInstance incoming, Entity source, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity entity = (LivingEntity) (Object) this;
		Holder<MobEffect> effect = incoming.getEffect();
		Holder<MobEffect> vanilla = vanillaCounterpart(effect);
		if (vanilla != null) {
			entity.removeEffect(vanilla);
			return;
		}

		Holder<MobEffect> supreme = supremeCounterpart(effect);
		if (supreme != null && entity.hasEffect(supreme)) {
			cir.setReturnValue(false);
		}
	}

	private static Holder<MobEffect> vanillaCounterpart(Holder<MobEffect> effect) {
		if (effect.equals(ModEffects.SUPREME_FIRE_RESISTANCE)) return MobEffects.FIRE_RESISTANCE;
		if (effect.equals(ModEffects.SUPREME_NIGHT_VISION)) return MobEffects.NIGHT_VISION;
		if (effect.equals(ModEffects.SUPREME_INVISIBILITY)) return MobEffects.INVISIBILITY;
		if (effect.equals(ModEffects.SUPREME_SLOW_FALLING)) return MobEffects.SLOW_FALLING;
		return null;
	}

	private static Holder<MobEffect> supremeCounterpart(Holder<MobEffect> effect) {
		if (effect.equals(MobEffects.FIRE_RESISTANCE)) return ModEffects.SUPREME_FIRE_RESISTANCE;
		if (effect.equals(MobEffects.NIGHT_VISION)) return ModEffects.SUPREME_NIGHT_VISION;
		if (effect.equals(MobEffects.INVISIBILITY)) return ModEffects.SUPREME_INVISIBILITY;
		if (effect.equals(MobEffects.SLOW_FALLING)) return ModEffects.SUPREME_SLOW_FALLING;
		return null;
	}
}
