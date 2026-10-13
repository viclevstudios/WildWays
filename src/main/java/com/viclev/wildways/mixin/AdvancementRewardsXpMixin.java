package com.viclev.wildways.mixin;

import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdvancementRewards.class)
public abstract class AdvancementRewardsXpMixin {
	@Shadow @Final private int experience;

	@Inject(method = "grant", at = @At("HEAD"))
	private void wildways$doubleAdvancementExperience(ServerPlayer player, CallbackInfo ci) {
		if (this.experience > 0) {
			player.giveExperiencePoints(this.experience);
		}
	}
}
