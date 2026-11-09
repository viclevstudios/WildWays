package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.viclev.wildways.PlantGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.entity.animal.bee.Bee$BeeGrowCropGoal")
abstract class PlantBeePollinationMixin {
	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target =
		"Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
	private boolean wildways$rollOnPollination(Level level, BlockPos pos, BlockState next, Operation<Boolean> original) {
		if (level instanceof ServerLevel serverLevel) {
			next = PlantGrowth.rollNaturalRipening(serverLevel, pos, next, serverLevel.getRandom());
		}
		return original.call(level, pos, next);
	}
}
