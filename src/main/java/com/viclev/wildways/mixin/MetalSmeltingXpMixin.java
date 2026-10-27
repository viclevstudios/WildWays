package com.viclev.wildways.mixin;

import com.viclev.wildways.XpProgression;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.BlastFurnaceMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.SmokerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FurnaceResultSlot.class)
public abstract class MetalSmeltingXpMixin {
	@Inject(method = "onTake", at = @At("TAIL"))
	private void wildways$rewardMetalSmelting(Player player, ItemStack result, CallbackInfo ci) {
		XpProgression.awardMetalSmelting(player, result);
		if (player instanceof ServerPlayer serverPlayer) {
			if (serverPlayer.containerMenu instanceof BlastFurnaceMenu) {
				XpProgression.awardWorkstationOnce(serverPlayer, Blocks.BLAST_FURNACE);
			} else if (serverPlayer.containerMenu instanceof SmokerMenu) {
				XpProgression.awardWorkstationOnce(serverPlayer, Blocks.SMOKER);
			} else if (serverPlayer.containerMenu instanceof FurnaceMenu) {
				XpProgression.awardWorkstationOnce(serverPlayer, Blocks.FURNACE);
			}
		}
	}
}
