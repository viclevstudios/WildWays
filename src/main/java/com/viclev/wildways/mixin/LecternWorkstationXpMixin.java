package com.viclev.wildways.mixin;

import com.viclev.wildways.XpProgression;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LecternMenu.class)
public abstract class LecternWorkstationXpMixin {
	@Inject(method = "clickMenuButton", at = @At("TAIL"))
	private void wildways$rewardLecternOperation(net.minecraft.world.entity.player.Player player, int button,
		CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue() && player instanceof ServerPlayer serverPlayer) {
			XpProgression.awardWorkstationOnce(serverPlayer, Blocks.LECTERN);
		}
	}
}
