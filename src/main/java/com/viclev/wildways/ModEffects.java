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
	public static final Holder<MobEffect> SUPREME_FIRE_RESISTANCE = register("supreme_fire_resistance", SupremeMobEffect.Kind.FIRE_RESISTANCE, 0xE49A3A);
	public static final Holder<MobEffect> SUPREME_NIGHT_VISION = register("supreme_night_vision", SupremeMobEffect.Kind.NIGHT_VISION, 0x1F1FA1);
	public static final Holder<MobEffect> SUPREME_INVISIBILITY = register("supreme_invisibility", SupremeMobEffect.Kind.INVISIBILITY, 0x7F8392);
	public static final Holder<MobEffect> SUPREME_SLOW_FALLING = register("supreme_slow_falling", SupremeMobEffect.Kind.SLOW_FALLING, 0xF3CFB9);

	private ModEffects() {
	}

	public static void initialize() {
	}

	private static Holder<MobEffect> register(String path, SupremeMobEffect.Kind kind, int color) {
		return Registry.registerForHolder(
			BuiltInRegistries.MOB_EFFECT,
			ResourceKey.create(Registries.MOB_EFFECT, Wildways.id(path)),
			new SupremeMobEffect(kind, color)
		);
	}
}
