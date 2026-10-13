package com.viclev.wildways.mixin;

import com.viclev.wildways.XpProgression;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHook.class)
public abstract class FishingTreasureXpMixin {
	@Inject(method = "retrieve", at = @At("TAIL"))
	private void wildways$doubleFishingTreasureXp(net.minecraft.world.item.ItemStack rod, CallbackInfoReturnable<Integer> cir) {
		FishingHook hook = (FishingHook)(Object)this;
		if (cir.getReturnValue() <= 0 || !(hook.level() instanceof ServerLevel level)
			|| !(hook.getPlayerOwner() instanceof ServerPlayer player)) {
			return;
		}
		AABB area = hook.getBoundingBox().inflate(2.0);
		boolean treasure = level.getEntitiesOfClass(ItemEntity.class, area).stream()
			.anyMatch(item -> XpProgression.isFishingTreasure(item.getItem()));
		if (treasure) {
			// FishingHook awards 1-6 XP for this catch; add a matching amount for treasure loot.
			player.giveExperiencePoints(level.getRandom().nextInt(6) + 1);
		}
	}
}
