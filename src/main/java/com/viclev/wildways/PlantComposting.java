package com.viclev.wildways;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class PlantComposting {
	private PlantComposting() {
	}

	public static boolean isSnifferPlant(ItemStack stack) {
		return stack.is(Items.TORCHFLOWER) || stack.is(Items.PITCHER_PLANT) || stack.is(PlantBlocks.CYCAD);
	}

	public static boolean accepts(BlockState state, ItemStack stack) {
		return state.getValue(ComposterBlock.LEVEL) == 0 || isSnifferBatch(state) == isSnifferPlant(stack);
	}

	public static BlockState batchFor(BlockState state, ItemStack stack) {
		return isSnifferPlant(stack) ? state.setValue(PlantGrowth.SNIFFER_COMPOST, 1) : state;
	}

	public static boolean isSnifferBatch(BlockState state) {
		return state.getValue(PlantGrowth.SNIFFER_COMPOST) == 1;
	}

	public static Item output(BlockState state) {
		return isSnifferBatch(state) ? PlantBlocks.COMPOST_ITEM : Items.BONE_MEAL;
	}
}
