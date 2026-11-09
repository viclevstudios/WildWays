package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.viclev.wildways.PlantGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({CropBlock.class, PitcherCropBlock.class, StemBlock.class, SweetBerryBushBlock.class})
abstract class PlantGrowthMixin {
	@WrapOperation(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"))
	private int wildways$applyTillage(RandomSource random, int bound, Operation<Integer> original,
		BlockState state, ServerLevel level, BlockPos pos) {
		float factor = PlantGrowth.growthMultiplier(level, pos);
		return factor == 1 ? original.call(random, bound) : random.nextFloat() < factor / bound ? 0 : 1;
	}
}
