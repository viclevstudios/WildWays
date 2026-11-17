package com.viclev.wildways;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModStructureProcessors {
	private ModStructureProcessors() {
	}

	public static void initialize() {
		Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR,
			Wildways.id("quarantine_grounds_foundation"), QuarantineFoundationProcessor.MAP_CODEC);
		Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR,
			Wildways.id("quarantine_grounds_mossify_stairs"), MossifyStoneBrickStairsProcessor.MAP_CODEC);
		Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR,
			Wildways.id("quarantine_grounds_strip_spruce_log"), StripSpruceLogProcessor.MAP_CODEC);
	}
}
