package com.viclev.wildways.mixin;

import com.viclev.wildways.EnchantedBookDrops;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.raid.Raider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Raider.class)
public class RaiderDeathLootMixin {
	@Inject(method = "die", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/world/entity/raid/Raid;removeFromRaid(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/raid/Raider;Z)V"))
	private void wildways$rollRaidBook(DamageSource source, CallbackInfo ci) {
		EnchantedBookDrops.dropRaidBooksBeforeLeavingRaid((Raider)(Object)this, source);
	}
}
