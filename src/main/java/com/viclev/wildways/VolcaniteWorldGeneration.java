package com.viclev.wildways;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class VolcaniteWorldGeneration {
	private static final ResourceKey<PlacedFeature> ORE = ResourceKey.create(Registries.PLACED_FEATURE, Wildways.id("ore_volcanite"));

	private VolcaniteWorldGeneration() {
	}

	public static void initialize() {
		BiomeModifications.addFeature(
			BiomeSelectors.includeByKey(Biomes.BASALT_DELTAS),
			GenerationStep.Decoration.UNDERGROUND_ORES,
			ORE
		);
	}
}
