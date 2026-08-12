package com.viclev.wildways;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public final class ModBlocks {
	public static final Block ENDERMITE_BOX = register(
		ModBlockItemIds.ENDERMITE_BOX,
		EndermiteBoxBlock::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.SHULKER_BOX)
	);

	private ModBlocks() {
	}

	private static Block register(
		BlockItemId id,
		Function<BlockBehaviour.Properties, Block> blockFactory,
		BlockBehaviour.Properties properties
	) {
		Block block = Registry.register(BuiltInRegistries.BLOCK, id.block(), blockFactory.apply(properties.setId(id.block())));
		EndermiteBoxItem blockItem = new EndermiteBoxItem(block, new Item.Properties().stacksTo(1).useBlockDescriptionPrefix().setId(id.item()));
		Registry.register(BuiltInRegistries.ITEM, id.item(), blockItem);
		return block;
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
			.register(output -> output.accept(ENDERMITE_BOX));
	}
}
