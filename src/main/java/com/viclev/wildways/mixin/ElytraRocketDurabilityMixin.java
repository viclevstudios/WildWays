package com.viclev.wildways.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireworkRocketItem.class)
public abstract class ElytraRocketDurabilityMixin {
	private static final int WILDWAYS_ROCKET_DURABILITY_COST = 10;

	@Inject(method = "use", at = @At("TAIL"))
	private void wildways$damageElytraWhenBoosting(Level level, Player player, InteractionHand hand,
		CallbackInfoReturnable<?> cir) {
		if (!level.isClientSide() && player.isFallFlying()) {
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
}
