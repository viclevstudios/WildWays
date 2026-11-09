package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.viclev.wildways.PlantGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({CropBlock.class, StemBlock.class, SweetBerryBushBlock.class})
abstract class PlantNaturalRipeningMixin {
	@WrapOperation(method = "randomTick", at = @At(value = "INVOKE", target =
		"Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
	private boolean wildways$rollOnNaturalGrowth(ServerLevel level, BlockPos pos, BlockState next, int flags,
		Operation<Boolean> original, @Local(argsOnly = true) RandomSource random) {
		return original.call(level, pos, PlantGrowth.rollNaturalRipening(level, pos, next, random), flags);
	}
}
