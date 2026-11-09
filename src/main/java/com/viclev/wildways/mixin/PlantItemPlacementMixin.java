package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.viclev.wildways.PlantGrowth;
import com.viclev.wildways.PlantQuality;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockItem.class)
abstract class PlantItemPlacementMixin {
	@ModifyReturnValue(method = "getPlacementState", at = @At("RETURN"))
	private BlockState wildways$plantLevelTwo(BlockState state, BlockPlaceContext context) {
		return state != null && context.getItemInHand().has(PlantQuality.LEVEL_TWO)
			? state.trySetValue(PlantGrowth.QUALITY, PlantGrowth.LEVEL_TWO) : state;
	}
}
