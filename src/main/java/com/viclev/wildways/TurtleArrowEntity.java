package com.viclev.wildways;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/** A normal arrow with a 50% higher base-damage multiplier. */
public class TurtleArrowEntity extends WildwaysArrowEntity {
	public TurtleArrowEntity(EntityType<? extends Arrow> type, Level level) {
		super(type, level);
	}

	public TurtleArrowEntity(Level level, LivingEntity shooter, ItemStack stack, ItemStack weapon) {
		this(ModEntityTypes.TURTLE_ARROW, level);
		this.initializeFromShooter(shooter, stack, weapon);
		this.setBaseDamage(3.0D);
	}

	@Override
	protected void onHitEntity(EntityHitResult hitResult) {
		super.onHitEntity(hitResult);
		SpecialArrowAdvancements.triggerTakeAim(this.getOwner(), hitResult.getEntity(), this.damageSources().arrow(this, this.getOwner()));
	}
}
