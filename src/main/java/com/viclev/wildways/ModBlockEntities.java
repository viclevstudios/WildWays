package com.viclev.wildways;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Blocks;

public final class ModBlockEntities {
	public static final BlockEntityType<AnvilStorageBlockEntity> ANVIL_STORAGE = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		Wildways.id("anvil_storage"),
		FabricBlockEntityTypeBuilder.create(AnvilStorageBlockEntity::new, Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL).build()
	);
	public static final BlockEntityType<EndermiteBoxBlockEntity> ENDERMITE_BOX = Registry.register(
		BuiltInRegistries.BLOCK_ENTITY_TYPE,
		Wildways.id("endermite_box"),
		FabricBlockEntityTypeBuilder.create(EndermiteBoxBlockEntity::new, ModBlocks.ENDERMITE_BOX).build()
	);

	private ModBlockEntities() {
	}

	public static void initialize() {
	}
}
