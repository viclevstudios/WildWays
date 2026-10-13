package com.viclev.wildways.mixin;

import com.viclev.wildways.XpProgression;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TurtleEggBlock.class)
public abstract class TurtleEggHatchXpMixin {
	@Inject(method = "randomTick", at = @At("TAIL"))
	private void wildways$rewardTurtleHatch(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
		if (!level.getBlockState(pos).is(Blocks.TURTLE_EGG)) {
			XpProgression.awardEggHatch(level, pos, EntityTypes.TURTLE);
		}
	}
}
