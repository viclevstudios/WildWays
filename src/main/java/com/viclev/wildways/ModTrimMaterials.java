package com.viclev.wildways;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

public final class ModTrimMaterials {
	public static final ResourceKey<TrimMaterial> VOLCANITE = ResourceKey.create(Registries.TRIM_MATERIAL, Wildways.id("volcanite"));

	private ModTrimMaterials() {
	}
}
