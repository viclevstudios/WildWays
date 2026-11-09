package com.viclev.wildways;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public final class PlantGrowth {
	public static final int NORMAL = 0;
	public static final int LEVEL_TWO = 1;
	public static final int LEVEL_TWO_SEEDS = 2;
	public static final IntegerProperty QUALITY = IntegerProperty.create("wildways_quality", NORMAL, LEVEL_TWO_SEEDS);
	public static final IntegerProperty TILLAGE = IntegerProperty.create("wildways_tillage", 0, 6);
	public static final IntegerProperty COMPOST_GROWN = IntegerProperty.create("wildways_compost_grown", 0, 1);
	public static final IntegerProperty SNIFFER_COMPOST = IntegerProperty.create("wildways_sniffer_compost", 0, 1);
	private static final float[] GROWTH_MULTIPLIERS = {1, 0.7F, 0.8F, 0.9F, 1.2F, 1.4F, 1.6F};

	private PlantGrowth() {
	}

	public static void addProperties(Block block, StateDefinition.Builder<Block, BlockState> builder) {
		switch (block.getDescriptionId()) {
			case "block.minecraft.wheat", "block.minecraft.carrots", "block.minecraft.potatoes", "block.minecraft.beetroots",
				"block.minecraft.sweet_berry_bush", "block.minecraft.melon_stem", "block.minecraft.pumpkin_stem",
				"block.minecraft.attached_melon_stem", "block.minecraft.attached_pumpkin_stem",
				"block.minecraft.pumpkin", "block.minecraft.cake" -> builder.add(QUALITY);
			case "block.minecraft.melon" -> builder.add(QUALITY, COMPOST_GROWN);
			case "block.minecraft.farmland", "block.wildways.compost_farmland" -> builder.add(TILLAGE);
			case "block.minecraft.composter" -> builder.add(SNIFFER_COMPOST);
			default -> {
				if (block instanceof CandleCakeBlock) builder.add(QUALITY);
			}
		}
	}

	public static int hoeTier(Item hoe) {
		if (hoe == Items.WOODEN_HOE) return 1;
		if (hoe == Items.STONE_HOE) return 2;
		if (hoe == Items.COPPER_HOE) return 3;
		if (hoe == Items.IRON_HOE) return 0;
		if (hoe == Items.GOLDEN_HOE || hoe == ProgressionItems.QUARTZ_HOE || hoe == ProgressionItems.AMETHYST_HOE) return 4;
		if (hoe == Items.DIAMOND_HOE || hoe == ModItems.VOLCANITE_HOE) return 5;
		if (hoe == Items.NETHERITE_HOE) return 6;
		return -1;
	}

	public static float growthMultiplier(BlockGetter level, BlockPos cropPos) {
		return GROWTH_MULTIPLIERS[level.getBlockState(cropPos.below()).getValueOrElse(TILLAGE, 0)];
	}

	public static BlockState updateQuality(BlockState previous, BlockState next) {
		int quality = previous.getValueOrElse(QUALITY, NORMAL);
		if (previous.getBlock() != next.getBlock()) {
			if (isStemPair(previous, next, Blocks.MELON_STEM, Blocks.ATTACHED_MELON_STEM)
				|| isStemPair(previous, next, Blocks.PUMPKIN_STEM, Blocks.ATTACHED_PUMPKIN_STEM)
				|| previous.is(Blocks.CAKE) && next.getBlock() instanceof CandleCakeBlock
				|| previous.getBlock() instanceof CandleCakeBlock && next.is(Blocks.CAKE)) {
				return next.setValue(QUALITY, quality);
			}
			return next;
		}
		if (quality == LEVEL_TWO) {
			return next.setValue(QUALITY, LEVEL_TWO);
		}
		if (!isMature(next)) {
			return next.trySetValue(QUALITY, NORMAL);
		}
		return quality == LEVEL_TWO_SEEDS ? next.setValue(QUALITY, quality) : next;
	}

	public static BlockState rollNaturalRipening(ServerLevel level, BlockPos pos, BlockState next, RandomSource random) {
		if (next.getValueOrElse(QUALITY, LEVEL_TWO) != NORMAL || !isMature(next)) {
			return next;
		}
		BlockState previous = level.getBlockState(pos);
		if (!previous.is(next.getBlock()) || previous.getValue(QUALITY) != NORMAL || isMature(previous)
			|| !hasMatureSnifferNeighbor(level, pos)) {
			return next;
		}
		float chance = level.getBlockState(pos.below()).is(PlantBlocks.COMPOST_FARMLAND) ? 0.04F : 0.025F;
		return random.nextFloat() < chance ? next.setValue(QUALITY, LEVEL_TWO_SEEDS) : next;
	}

	private static boolean isStemPair(BlockState first, BlockState second, Block stem, Block attached) {
		return first.is(stem) && second.is(attached) || first.is(attached) && second.is(stem);
	}

	public static boolean isMature(BlockState state) {
		if (state.getBlock() instanceof CropBlock crop) return crop.isMaxAge(state);
		if (state.getBlock() instanceof StemBlock) return state.getValue(StemBlock.AGE) == StemBlock.MAX_AGE;
		if (state.is(Blocks.SWEET_BERRY_BUSH)) return state.getValue(SweetBerryBushBlock.AGE) == SweetBerryBushBlock.MAX_AGE;
		return state.is(Blocks.ATTACHED_MELON_STEM) || state.is(Blocks.ATTACHED_PUMPKIN_STEM);
	}

	private static boolean hasMatureSnifferNeighbor(BlockGetter level, BlockPos pos) {
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos adjacent = pos.relative(direction);
			BlockState state = level.getBlockState(adjacent);
			if (isPitcher(state) && state.getValue(PitcherCropBlock.AGE) == 4
				|| state.is(Blocks.TORCHFLOWER) && level.getBlockState(adjacent.below()).hasProperty(TILLAGE)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isPitcher(BlockState state) {
		return state.is(Blocks.PITCHER_CROP) && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER;
	}
}
