package com.viclev.wildways;

import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

/** Preserves vanilla projectile advancement checks for Wildways special arrows. */
final class SpecialArrowAdvancements {
	private SpecialArrowAdvancements() {
	}

	static void triggerTakeAim(Entity owner, Entity target, DamageSource arrowDamage) {
		if (owner instanceof ServerPlayer player) {
			CriteriaTriggers.PLAYER_HURT_ENTITY.trigger(player, target, arrowDamage, 1.0F, 1.0F, false);
		}
	}
}
