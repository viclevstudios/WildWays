package com.viclev.wildways;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public final class ModItems {
	public static final Item ENDERMITE_SHELL = register(
		ModItemIds.ENDERMITE_SHELL,
		Item::new,
		new Item.Properties()
	);
	public static final Item BIOME_COMPASS = register(
		ModItemIds.BIOME_COMPASS,
		Item::new,
		new Item.Properties()
	);

	private ModItems() {
	}

	private static <T extends Item> T register(
		ResourceKey<Item> itemKey,
		Function<Item.Properties, T> itemFactory,
		Item.Properties properties
	) {
		T item = itemFactory.apply(properties.setId(itemKey));
		return Registry.register(BuiltInRegistries.ITEM, itemKey, item);
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
			.register(output -> output.accept(ENDERMITE_SHELL));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
			.register(output -> output.accept(BIOME_COMPASS));
	}
}
