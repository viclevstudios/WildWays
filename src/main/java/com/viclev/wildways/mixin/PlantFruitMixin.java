package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.viclev.wildways.PlantBlocks;
import com.viclev.wildways.PlantGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StemBlock.class)
abstract class PlantFruitMixin {
	@WrapOperation(method = "randomTick", at = @At(value = "INVOKE", target =
		"Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 0))
	private boolean wildways$inheritFruitQuality(ServerLevel level, BlockPos fruitPos, BlockState fruit,
		Operation<Boolean> original, BlockState stem, ServerLevel ignored, BlockPos stemPos) {
		if (stem.getValueOrElse(PlantGrowth.QUALITY, PlantGrowth.NORMAL) == PlantGrowth.LEVEL_TWO) {
			fruit = fruit.trySetValue(PlantGrowth.QUALITY, PlantGrowth.LEVEL_TWO);
		}
		if (level.getBlockState(stemPos.below()).is(PlantBlocks.COMPOST_FARMLAND)) {
			fruit = fruit.trySetValue(PlantGrowth.COMPOST_GROWN, 1);
		}
		return original.call(level, fruitPos, fruit);
	}
}
