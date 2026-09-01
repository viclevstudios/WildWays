package com.viclev.wildways;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class ModItemIds {
	public static final ResourceKey<Item> ENDERMITE_SHELL = create("endermite_shell");
	public static final ResourceKey<Item> BIOME_COMPASS = create("biome_compass");
	public static final ResourceKey<Item> TURTLE_ARROW = create("turtle_arrow");
	public static final ResourceKey<Item> RANGE_ARROW = create("range_arrow");
	public static final ResourceKey<Item> EXPLOSIVE_ARROW = create("explosive_arrow");

	private ModItemIds() {
	}

	private static ResourceKey<Item> create(String path) {
		return ResourceKey.create(Registries.ITEM, Wildways.id(path));
	}
}
