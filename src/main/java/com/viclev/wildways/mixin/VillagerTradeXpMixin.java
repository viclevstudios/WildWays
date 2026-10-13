package com.viclev.wildways.mixin;

import com.viclev.wildways.XpProgression;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantResultSlot.class)
public abstract class VillagerTradeXpMixin {
	@Shadow @Final private Merchant merchant;

	@Inject(method = "onTake", at = @At("TAIL"))
	private void wildways$rewardFirstVillagerTrade(Player player, ItemStack result, CallbackInfo ci) {
		if (this.merchant instanceof Villager && player instanceof ServerPlayer serverPlayer && !result.isEmpty()) {
			XpProgression.awardFirstVillagerTrade(serverPlayer);
		}
	}
}
