package com.viclev.wildways;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/** Replaces stone-brick stairs with mossy variants without changing their direction, half, or shape. */
public final class MossifyStoneBrickStairsProcessor implements StructureProcessor {
	public static final MapCodec<MossifyStoneBrickStairsProcessor> MAP_CODEC = MapCodec.unit(new MossifyStoneBrickStairsProcessor());

	@Override
	public MapCodec<MossifyStoneBrickStairsProcessor> codec() {
		return MAP_CODEC;
	}

	@Override
	public StructureTemplate.StructureBlockInfo processBlock(
		LevelReader level,
		BlockPos templateOrigin,
		BlockPos referencePos,
		BlockPos sourcePos,
		StructureTemplate.StructureBlockInfo blockInfo,
		StructurePlaceSettings settings
	) {
		if (!blockInfo.state().is(Blocks.STONE_BRICK_STAIRS) || settings.getRandom(blockInfo.pos()).nextFloat() >= 0.3F) {
			return blockInfo;
		}

		BlockState mossyState = copySharedProperties(blockInfo.state(), Blocks.MOSSY_STONE_BRICK_STAIRS.defaultBlockState());
		return new StructureTemplate.StructureBlockInfo(blockInfo.pos(), mossyState, blockInfo.nbt());
	}

	private static BlockState copySharedProperties(BlockState original, BlockState replacement) {
		BlockState result = replacement;
		for (Property<?> property : original.getProperties()) {
			if (result.hasProperty(property)) {
				result = copyProperty(original, result, property);
			}
		}
		return result;
	}

	private static <T extends Comparable<T>> BlockState copyProperty(BlockState original, BlockState replacement, Property<T> property) {
		return replacement.setValue(property, original.getValue(property));
	}
}
