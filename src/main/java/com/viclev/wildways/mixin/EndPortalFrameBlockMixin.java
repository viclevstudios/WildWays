package com.viclev.wildways.mixin;

import com.viclev.wildways.PortalEyeStates;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndPortalFrameBlock.class)
public abstract class EndPortalFrameBlockMixin {
	@Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
	private void wildways$addEyeType(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
		builder.add(PortalEyeStates.EYE_TYPE);
	}
}
