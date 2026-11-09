package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.viclev.wildways.PlantGrowth;
import com.viclev.wildways.PlantQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CakeBlock.class)
abstract class PlantCakeMixin {
	@WrapOperation(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(IF)V"))
	private static void wildways$eatLevelTwoCake(FoodData food, int hunger, float saturation, Operation<Void> original,
		LevelAccessor level, BlockPos pos, BlockState state) {
		if (state.getValue(PlantGrowth.QUALITY) == PlantGrowth.LEVEL_TWO) {
			food.eat(PlantQuality.CAKE_SLICE);
		} else {
			original.call(food, hunger, saturation);
		}
	}
}
