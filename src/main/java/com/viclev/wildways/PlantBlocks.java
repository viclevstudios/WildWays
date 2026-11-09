package com.viclev.wildways;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class PlantBlocks {
	public static final Block COMPOST = registerBlock("compost", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT));
	public static final Block COMPOST_FARMLAND = registerBlock("compost_farmland", properties -> new FarmlandBlock(COMPOST, properties),
		BlockBehaviour.Properties.ofFullCopy(Blocks.FARMLAND));
	public static final Item COMPOST_ITEM = registerItem("compost", COMPOST);
	public static final Block CYCAD_CROP = registerBlock("cycad", CycadCropBlock::new,
		BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT));
	public static final Item CYCAD = registerItem("cycad", CYCAD_CROP);

	private PlantBlocks() {
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS).register(output -> {
			output.accept(COMPOST_ITEM);
			output.accept(CYCAD);
		});
	}

	private static Block registerBlock(String path, java.util.function.Function<BlockBehaviour.Properties, Block> factory,
		BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Wildways.id(path));
		return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
	}

	private static Item registerItem(String path, Block block) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Wildways.id(path));
		Item.Properties properties = new Item.Properties().setId(key);
		if (block instanceof CropBlock) {
			properties.compostable(ResourceKey.create(Registries.CONTEXT_INT_PROVIDER, Wildways.id("cycad_composting")));
		}
		return Registry.register(BuiltInRegistries.ITEM, key, new BlockItem(block, properties));
	}

	private static final class CycadCropBlock extends CropBlock {
		private CycadCropBlock(BlockBehaviour.Properties properties) {
			super(properties);
		}

		@Override
		protected ItemLike getBaseSeedId() {
			return CYCAD;
		}
	}
}
