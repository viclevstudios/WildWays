package com.viclev.wildways;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class ModItemIds {
	public static final ResourceKey<Item> ENDERMITE_SHELL = create("endermite_shell");

	private ModItemIds() {
	}

	private static ResourceKey<Item> create(String path) {
		return ResourceKey.create(Registries.ITEM, Wildways.id(path));
	}
}
