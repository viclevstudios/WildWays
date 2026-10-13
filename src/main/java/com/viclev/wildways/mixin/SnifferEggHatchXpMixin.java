package com.viclev.wildways.mixin;

import com.viclev.wildways.XpProgression;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnifferEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnifferEggBlock.class)
public abstract class SnifferEggHatchXpMixin {
	@Inject(method = "tick", at = @At("TAIL"))
	private void wildways$rewardSnifferHatch(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
		if (!level.getBlockState(pos).is(Blocks.SNIFFER_EGG)) {
			XpProgression.awardEggHatch(level, pos, EntityTypes.SNIFFER);
		}
	}
}
