package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.viclev.wildways.PlantGrowth;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CandleCakeBlock.class)
abstract class PlantCandleCakeMixin {
	@ModifyExpressionValue(method = "useWithoutItem", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/world/level/block/Block;defaultBlockState()Lnet/minecraft/world/level/block/state/BlockState;"))
	private BlockState wildways$preserveCakeLevel(BlockState cake, BlockState candleCake) {
		return cake.setValue(PlantGrowth.QUALITY, candleCake.getValue(PlantGrowth.QUALITY));
	}
}
