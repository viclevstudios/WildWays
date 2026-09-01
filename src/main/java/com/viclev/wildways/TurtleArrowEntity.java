package com.viclev.wildways;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A normal arrow with a 50% higher base-damage multiplier. */
public class TurtleArrowEntity extends Arrow {
	public TurtleArrowEntity(Level level, LivingEntity shooter, ItemStack stack, ItemStack weapon) {
		super(level, shooter, stack, weapon);
		this.setBaseDamage(3.0D);
	}
}
