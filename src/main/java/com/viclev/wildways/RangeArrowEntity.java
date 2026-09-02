package com.viclev.wildways;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/** Projectile launch speed is scaled here, so bows and crossbows use the same rule. */
public class RangeArrowEntity extends WildwaysArrowEntity {
	public RangeArrowEntity(EntityType<? extends Arrow> type, Level level) {
		super(type, level);
	}

	public RangeArrowEntity(Level level, LivingEntity shooter, ItemStack stack, ItemStack weapon) {
		this(ModEntityTypes.RANGE_ARROW, level);
		this.initializeFromShooter(shooter, stack, weapon);
		// Arrow damage scales with flight speed. Compensate the 1.5x launch speed
		// so this arrow still deals the same damage as a normal arrow.
		this.setBaseDamage(4.0D / 3.0D);
	}

	@Override
	public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
		super.shoot(x, y, z, velocity * 1.5F, inaccuracy);
	}

	@Override
	protected void onHitEntity(EntityHitResult hitResult) {
		super.onHitEntity(hitResult);
		SpecialArrowAdvancements.triggerTakeAim(this.getOwner(), hitResult.getEntity(), this.damageSources().arrow(this, this.getOwner()));
	}
}
