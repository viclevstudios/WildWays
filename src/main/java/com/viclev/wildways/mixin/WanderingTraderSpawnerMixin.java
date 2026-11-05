package com.viclev.wildways.mixin;

import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(WanderingTraderSpawner.class)
public abstract class WanderingTraderSpawnerMixin {
	@ModifyConstant(method = {"<init>", "tick"}, constant = @Constant(intValue = 1200, ordinal = 0))
	private int wildways$checkMoreOften(int interval) {
		// Keep the saved delay and its decrement in vanilla units. Only shorten the time between decrements.
		return 800;
	}
}
