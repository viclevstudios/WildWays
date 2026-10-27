package com.viclev.wildways.mixin;

import com.viclev.wildways.XpProgression;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentMenu.class)
public abstract class VanillaEnchantingWorkstationXpMixin {
	@Inject(method = "clickMenuButton", at = @At("TAIL"))
	private void wildways$rewardVanillaEnchantment(net.minecraft.world.entity.player.Player player, int button,
		CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue() && player instanceof ServerPlayer serverPlayer) {
			XpProgression.awardWorkstationOnce(serverPlayer, Blocks.ENCHANTING_TABLE);
		}
	}
}
