package com.viclev.wildways;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/** A block-breaking arrow explosion with normal mob-griefing behaviour. */
public class ExplosiveArrowEntity extends Arrow {
	public ExplosiveArrowEntity(Level level, LivingEntity shooter, ItemStack stack, ItemStack weapon) {
		super(level, shooter, stack, weapon);
	}

	@Override
	protected void onHit(HitResult hitResult) {
		if (!this.level().isClientSide()) {
			this.level().explode(this, this.getX(), this.getY(), this.getZ(), 2.0F, false, Level.ExplosionInteraction.MOB);
			this.discard();
		}
	}
}
