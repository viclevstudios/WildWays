package com.viclev.wildways;

import com.viclev.wildways.mixin.AbstractArrowAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

/** Common launch setup for the three custom arrow entity types. */
public abstract class WildwaysArrowEntity extends Arrow {
	protected WildwaysArrowEntity(EntityType<? extends Arrow> type, Level level) {
		super(type, level);
	}

	protected void initializeFromShooter(LivingEntity shooter, ItemStack projectile, ItemStack weapon) {
		this.setPickupItemStack(projectile.copy());
		this.setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
		this.setOwner(shooter);
		if (!weapon.isEmpty() && this.level() instanceof ServerLevel serverLevel) {
			AbstractArrowAccessor accessor = (AbstractArrowAccessor) this;
			accessor.wildways$setFiredFromWeapon(weapon.copy());
			int piercing = EnchantmentHelper.getPiercingCount(serverLevel, weapon, projectile);
			if (piercing > 0) {
				accessor.wildways$setPierceLevel((byte) piercing);
			}
		}
	}
}
