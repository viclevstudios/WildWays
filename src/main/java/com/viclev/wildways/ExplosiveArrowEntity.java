package com.viclev.wildways;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/** A block-breaking arrow explosion with normal mob-griefing behaviour. */
public class ExplosiveArrowEntity extends WildwaysArrowEntity {
	private static final ExplosionDamageCalculator REDUCED_ENTITY_DAMAGE = new ExplosionDamageCalculator() {
		@Override
		public float getEntityDamageAmount(Explosion explosion, net.minecraft.world.entity.Entity entity, float exposure) {
			return super.getEntityDamageAmount(explosion, entity, exposure) * 0.70F;
		}
	};

	public ExplosiveArrowEntity(EntityType<? extends Arrow> type, Level level) {
		super(type, level);
	}

	public ExplosiveArrowEntity(Level level, LivingEntity shooter, ItemStack stack, ItemStack weapon) {
		this(ModEntityTypes.EXPLOSIVE_ARROW, level);
		this.initializeFromShooter(shooter, stack, weapon);
	}

	@Override
	protected void onHit(HitResult hitResult) {
		if (!this.level().isClientSide()) {
			if (hitResult instanceof net.minecraft.world.phys.EntityHitResult entityHit) {
				SpecialArrowAdvancements.triggerTakeAim(this.getOwner(), entityHit.getEntity(), this.damageSources().arrow(this, this.getOwner()));
			}
			this.level().explode(
				this,
				Explosion.getDefaultDamageSource(this.level(), this),
				REDUCED_ENTITY_DAMAGE,
				this.getX(), this.getY(), this.getZ(),
				1.5F,
				false,
				Level.ExplosionInteraction.MOB
			);
			this.discard();
		}
	}
}
