package com.viclev.wildways;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;

public final class ModMenuTypes {
	public static final MenuType<WildwaysEnchantingMenu> ENCHANTING = Registry.register(
		BuiltInRegistries.MENU,
		Wildways.id("enchanting"),
		new MenuType<>(WildwaysEnchantingMenu::new, FeatureFlagSet.of())
	);
	public static final MenuType<EndermiteBoxMenu> ENDERMITE_BOX = Registry.register(
		BuiltInRegistries.MENU,
		Wildways.id("endermite_box"),
		new MenuType<>(EndermiteBoxMenu::new, FeatureFlagSet.of())
	);
	public static final MenuType<FletchingTableMenu> FLETCHING_TABLE = Registry.register(
		BuiltInRegistries.MENU,
		Wildways.id("fletching_table"),
		new MenuType<>(FletchingTableMenu::new, FeatureFlagSet.of())
	);
	public static final MenuType<BiomeCompassMenu> BIOME_COMPASS = Registry.register(
		BuiltInRegistries.MENU,
		Wildways.id("biome_compass"),
		new ExtendedMenuType<>(BiomeCompassMenu::new, Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()))
	);

	private ModMenuTypes() {
	}

	public static void initialize() {
	}
}
