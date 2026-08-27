package com.viclev.wildways;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModStructureProcessors {
	public static final MapCodec<QuarantineFoundationProcessor> FOUNDATION = Registry.register(
		BuiltInRegistries.STRUCTURE_PROCESSOR,
		Wildways.id("quarantine_grounds_foundation"),
		QuarantineFoundationProcessor.MAP_CODEC
	);
	public static final MapCodec<MossifyStoneBrickStairsProcessor> MOSSIFY_STONE_BRICK_STAIRS = Registry.register(
		BuiltInRegistries.STRUCTURE_PROCESSOR,
		Wildways.id("quarantine_grounds_mossify_stairs"),
		MossifyStoneBrickStairsProcessor.MAP_CODEC
	);

	private ModStructureProcessors() {
	}

	public static void initialize() {
	}
}
