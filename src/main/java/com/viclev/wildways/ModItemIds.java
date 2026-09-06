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
	public static final ResourceKey<Item> EYE_OF_ICE = create("eye_of_ice");
	public static final ResourceKey<Item> EYE_OF_THE_BRUTE = create("eye_of_the_brute");
	public static final ResourceKey<Item> EYE_OF_STORM = create("eye_of_storm");
	public static final ResourceKey<Item> EYE_OF_WATER = create("eye_of_water");
	public static final ResourceKey<Item> LOST_EYE = create("lost_eye");
	public static final ResourceKey<Item> EYE_OF_ILLAGERS = create("eye_of_illagers");
	public static final ResourceKey<Item> EYE_OF_THE_TIGER = create("eye_of_the_tiger");
	public static final ResourceKey<Item> EYE_OF_DARKNESS = create("eye_of_darkness");
	public static final ResourceKey<Item> EYE_OF_THE_CREAKING = create("eye_of_the_creaking");
	public static final ResourceKey<Item> EYE_OF_BREWING = create("eye_of_brewing");
	public static final ResourceKey<Item> ENCHANTED_EYE = create("enchanted_eye");
	public static final ResourceKey<Item> EYE_OF_ENDERMITES = create("eye_of_endermites");
	public static final ResourceKey<Item> AWKWARD_EYE = create("awkward_eye");
	public static final ResourceKey<Item> THICK_EYE = create("thick_eye");

	private ModItemIds() {
	}

	private static ResourceKey<Item> create(String path) {
		return ResourceKey.create(Registries.ITEM, Wildways.id(path));
	}
}
