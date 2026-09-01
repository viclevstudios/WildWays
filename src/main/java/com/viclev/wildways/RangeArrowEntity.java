package com.viclev.wildways;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Projectile launch speed is scaled here, so bows and crossbows use the same rule. */
public class RangeArrowEntity extends Arrow {
	public RangeArrowEntity(Level level, LivingEntity shooter, ItemStack stack, ItemStack weapon) {
		super(level, shooter, stack, weapon);
		// Arrow damage scales with flight speed. Compensate the 1.5x launch speed
		// so this arrow still deals the same damage as a normal arrow.
		this.setBaseDamage(4.0D / 3.0D);
	}

	@Override
	public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
		super.shoot(x, y, z, velocity * 1.5F, inaccuracy);
	}
}
