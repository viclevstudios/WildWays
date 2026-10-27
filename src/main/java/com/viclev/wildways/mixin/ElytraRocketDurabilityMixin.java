package com.viclev.wildways.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireworkRocketEntity.class)
public abstract class ElytraRocketDurabilityMixin {
	private static final int WILDWAYS_ROCKET_DURABILITY_COST = 5;

	@Shadow private LivingEntity attachedToEntity;
	@Unique private boolean wildways$elytraDurabilityApplied;

	@Inject(method = "tick", at = @At("HEAD"))
	private void wildways$damageElytraWhenRocketBoostBegins(CallbackInfo ci) {
		if (this.wildways$elytraDurabilityApplied || attachedToEntity == null
			|| attachedToEntity.level().isClientSide() || !(attachedToEntity instanceof Player player)
			|| !player.isFallFlying()) {
			return;
		}

		this.wildways$elytraDurabilityApplied = true;
		ItemStack elytra = player.getItemBySlot(EquipmentSlot.CHEST);
		if (elytra.has(DataComponents.GLIDER) && elytra.isDamageableItem()) {
			int remainingDurability = elytra.getMaxDamage() - elytra.getDamageValue();
			int damage = Math.min(WILDWAYS_ROCKET_DURABILITY_COST, Math.max(0, remainingDurability - 1));
			if (damage > 0) {
				elytra.hurtAndBreak(damage, player, EquipmentSlot.CHEST);
			}
		}
	}
}
