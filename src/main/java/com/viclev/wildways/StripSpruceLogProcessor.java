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

/** Weathered hospital spruce logs become stripped while keeping their original axis. */
public final class StripSpruceLogProcessor implements StructureProcessor {
	public static final MapCodec<StripSpruceLogProcessor> MAP_CODEC = MapCodec.unit(new StripSpruceLogProcessor());

	@Override
	public MapCodec<StripSpruceLogProcessor> codec() {
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
		if (!blockInfo.state().is(Blocks.SPRUCE_LOG) || settings.getRandom(blockInfo.pos()).nextFloat() >= 0.1F) {
			return blockInfo;
		}

		BlockState strippedState = copySharedProperties(blockInfo.state(), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
		return new StructureTemplate.StructureBlockInfo(blockInfo.pos(), strippedState, blockInfo.nbt());
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
