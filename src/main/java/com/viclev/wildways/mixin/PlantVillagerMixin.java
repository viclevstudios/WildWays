package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.viclev.wildways.PlantGrowth;
import com.viclev.wildways.PlantQuality;
import net.minecraft.world.entity.ai.behavior.HarvestFarmland;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HarvestFarmland.class)
abstract class PlantVillagerMixin {
	@ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/world/level/block/Block;defaultBlockState()Lnet/minecraft/world/level/block/state/BlockState;"))
	private BlockState wildways$plantLevelTwo(BlockState state, @Local ItemStack seeds) {
		return seeds.has(PlantQuality.LEVEL_TWO) ? state.trySetValue(PlantGrowth.QUALITY, PlantGrowth.LEVEL_TWO) : state;
	}
}
