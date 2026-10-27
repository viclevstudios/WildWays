package com.viclev.wildways.mixin;

import com.viclev.wildways.ElytraDurability;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireworkRocketItem.class)
public abstract class ElytraRocketUseMixin {
	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	private void wildways$preventBoostWithExhaustedGlider(Level level, Player player, InteractionHand hand,
		CallbackInfoReturnable<InteractionResult> cir) {
		if (player.isFallFlying() && ElytraDurability.isAtLastPoint(player.getItemBySlot(EquipmentSlot.CHEST))) {
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}
}
