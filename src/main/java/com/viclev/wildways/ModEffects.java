package com.viclev.wildways;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;

public final class ModEffects {
	public static final Holder<MobEffect> UNEASE = Registry.registerForHolder(
		BuiltInRegistries.MOB_EFFECT,
		ResourceKey.create(Registries.MOB_EFFECT, Wildways.id("unease")),
		new UneaseMobEffect()
	);

	private ModEffects() {
	}

	public static void initialize() {
	}
}
