package com.viclev.wildways.mixin;

import com.viclev.wildways.PlantGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Level.class)
abstract class PlantQualityGrowthMixin {
	@ModifyVariable(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
		at = @At("HEAD"), argsOnly = true)
	private BlockState wildways$carryPlantQuality(BlockState state, BlockPos pos) {
		if (state.hasProperty(PlantGrowth.QUALITY) && (Object) this instanceof ServerLevel level && level.isInValidBounds(pos)) {
			return PlantGrowth.updateQuality(level.getBlockState(pos), state);
		}
		return state;
	}
}
