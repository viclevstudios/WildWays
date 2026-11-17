package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.viclev.wildways.XpProgression;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeehiveBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BeehiveBlock.class)
public class BeehiveHarvestXpMixin {
	@WrapOperation(method = "useItemOn", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/world/entity/player/Player;awardStat(Lnet/minecraft/stats/Stat;)V"))
	private void wildways$awardHarvestXp(Player player, Stat<?> stat, Operation<Void> original) {
		original.call(player, stat);
		if (player instanceof ServerPlayer serverPlayer) {
			XpProgression.awardHoneyHarvest(serverPlayer, stat.getValue() == Items.SHEARS);
		}
	}
}
