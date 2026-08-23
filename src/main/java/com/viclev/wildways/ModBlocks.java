package com.viclev.wildways;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Function;

public final class ModBlocks {
	public static final Block ENDERMITE_BOX = register(
		ModBlockItemIds.ENDERMITE_BOX,
		EndermiteBoxBlock::new,
		BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_PURPLE)
			.strength(2.0F)
			.sound(SoundType.STONE)
			.noOcclusion()
			.pushReaction(PushReaction.DESTROY)
	);
	public static final Block ENDERMITE_BRICKS = registerBlock(
		ModBlockItemIds.ENDERMITE_BRICKS,
		Block::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)
	);
	public static final Block ENDERMITE_BRICK_WALL = registerBlock(
		ModBlockItemIds.ENDERMITE_BRICK_WALL,
		WallBlock::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_WALL)
	);
	public static final Block ENDERMITE_BRICK_STAIRS = registerBlock(
		ModBlockItemIds.ENDERMITE_BRICK_STAIRS,
		properties -> new StairBlock(ENDERMITE_BRICKS.defaultBlockState(), properties),
		BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_STAIRS)
	);
	public static final Block ENDERMITE_BRICK_SLAB = registerBlock(
		ModBlockItemIds.ENDERMITE_BRICK_SLAB,
		SlabBlock::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_SLAB)
	);
	public static final Block LIGHT_SENSOR = registerBlock(
		ModBlockItemIds.LIGHT_SENSOR,
		LightSensorBlock::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.DAYLIGHT_DETECTOR)
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

	private static Block registerBlock(
		BlockItemId id,
		Function<BlockBehaviour.Properties, Block> blockFactory,
		BlockBehaviour.Properties properties
	) {
		Block block = Registry.register(BuiltInRegistries.BLOCK, id.block(), blockFactory.apply(properties.setId(id.block())));
		Registry.register(BuiltInRegistries.ITEM, id.item(), new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(id.item())));
		return block;
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
			.register(output -> {
				output.accept(ENDERMITE_BOX);
				output.accept(LIGHT_SENSOR);
			});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS)
			.register(output -> {
				output.accept(ENDERMITE_BRICKS);
				output.accept(ENDERMITE_BRICK_WALL);
				output.accept(ENDERMITE_BRICK_STAIRS);
				output.accept(ENDERMITE_BRICK_SLAB);
			});
	}
}
