package com.viclev.wildways.mixin;

import com.viclev.wildways.PlantGrowth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Block.class)
abstract class PlantStateDefinitionMixin {
	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target =
		"Lnet/minecraft/world/level/block/Block;createBlockStateDefinition(Lnet/minecraft/world/level/block/state/StateDefinition$Builder;)V"))
	private StateDefinition.Builder<Block, BlockState> wildways$addPlantProperties(StateDefinition.Builder<Block, BlockState> builder) {
		PlantGrowth.addProperties((Block) (Object) this, builder);
		return builder;
	}
}
