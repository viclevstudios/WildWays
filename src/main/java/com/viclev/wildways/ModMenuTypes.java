package com.viclev.wildways;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;

public final class ModMenuTypes {
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

	private ModMenuTypes() {
	}

	public static void initialize() {
	}
}
