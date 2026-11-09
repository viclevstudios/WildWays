package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.TorchflowerCropBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CropBlock.class)
abstract class TorchflowerLightRequirementsMixin {
	@WrapOperation(method = "canSurvive", at = @At(value = "INVOKE", target =
		"Lnet/minecraft/world/level/block/CropBlock;hasSufficientLight(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"))
	private boolean wildways$allowTorchflowersInDarkness(LevelReader level, BlockPos pos, Operation<Boolean> original) {
		return (Object) this instanceof TorchflowerCropBlock || original.call(level, pos);
	}

	@WrapOperation(method = "randomTick", at = @At(value = "INVOKE", target =
		"Lnet/minecraft/server/level/ServerLevel;getRawBrightness(Lnet/minecraft/core/BlockPos;I)I"))
	private int wildways$growTorchflowersInDarkness(ServerLevel level, BlockPos pos, int skyDarken, Operation<Integer> original) {
		return (Object) this instanceof TorchflowerCropBlock ? 9 : original.call(level, pos, skyDarken);
	}
}
