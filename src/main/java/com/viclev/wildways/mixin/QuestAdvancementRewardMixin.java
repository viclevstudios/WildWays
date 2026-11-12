package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.viclev.wildways.QuestRewards;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerAdvancements.class)
abstract class QuestAdvancementRewardMixin {
	@WrapOperation(method = "award", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/advancements/AdvancementRewards;grant(Lnet/minecraft/server/level/ServerPlayer;)V"))
	private void wildways$rewardGuideQuest(AdvancementRewards rewards, ServerPlayer player, Operation<Void> original,
		@Local(argsOnly = true) AdvancementHolder advancement) {
		original.call(rewards, player);
		QuestRewards.complete(player, advancement.id());
	}
}
