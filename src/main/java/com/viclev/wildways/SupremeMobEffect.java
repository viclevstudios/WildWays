package com.viclev.wildways;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/** Small effect hooks that cannot be expressed by potion data alone. */
public class SupremeMobEffect extends MobEffect {
	private final Kind kind;

	public SupremeMobEffect(Kind kind, int color) {
		super(MobEffectCategory.BENEFICIAL, color);
		this.kind = kind;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}

	@Override
	public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
		switch (this.kind) {
			case FIRE_RESISTANCE -> entity.clearFire();
			case NIGHT_VISION -> {
				for (Entity nearby : level.getEntities(entity, entity.getBoundingBox().inflate(15.0D), candidate -> candidate instanceof LivingEntity)) {
					((LivingEntity) nearby).addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, true, false, false), entity);
				}
			}
			case INVISIBILITY -> {
			}
			case SLOW_FALLING -> entity.resetFallDistance();
		}
		return true;
	}

	public enum Kind {
		FIRE_RESISTANCE,
		NIGHT_VISION,
		INVISIBILITY,
		SLOW_FALLING
	}
}
