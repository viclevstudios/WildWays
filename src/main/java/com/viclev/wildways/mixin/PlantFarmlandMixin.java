package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.viclev.wildways.PlantGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FarmlandBlock.class)
abstract class PlantFarmlandMixin {
	@ModifyReturnValue(method = "isNearWater", at = @At("RETURN"))
	private static boolean wildways$hydrateFromPitcher(boolean nearWater, LevelReader level, BlockPos pos) {
		if (nearWater) return true;
		BlockPos.MutableBlockPos nearby = new BlockPos.MutableBlockPos();
		for (int x = -3; x <= 3; x++) {
			for (int z = -3; z <= 3; z++) {
				nearby.setWithOffset(pos, x, 1, z);
				BlockState plant = level.getBlockState(nearby);
				if (PlantGrowth.isPitcher(plant)
					|| (plant.is(Blocks.PITCHER_PLANT) && plant.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER)) {
					return true;
				}
			}
		}
		return false;
	}
}
