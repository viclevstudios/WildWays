package com.viclev.wildways.mixin;

import com.viclev.wildways.ModEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Gives Supreme Fire Resistance the complete vanilla fire-damage immunity itself. */
@Mixin(LivingEntity.class)
public class SupremeFireResistanceMixin {
	@Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
	private void wildways$blockFireDamage(ServerLevel level, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self.hasEffect(ModEffects.SUPREME_FIRE_RESISTANCE) && source.is(DamageTypeTags.IS_FIRE)) {
			cir.setReturnValue(true);
		}
	}
}
