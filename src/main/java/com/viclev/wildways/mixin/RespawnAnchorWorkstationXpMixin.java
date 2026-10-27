package com.viclev.wildways.mixin;

import com.viclev.wildways.XpProgression;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RespawnAnchorBlock.class)
public abstract class RespawnAnchorWorkstationXpMixin {
	@Inject(method = "charge", at = @At("TAIL"))
	private static void wildways$rewardChargingAnchor(Entity source, Level level, BlockPos pos, BlockState state, CallbackInfo ci) {
		if (source instanceof ServerPlayer serverPlayer && level instanceof ServerLevel
			&& level.getBlockState(pos).getValue(RespawnAnchorBlock.CHARGE) > state.getValue(RespawnAnchorBlock.CHARGE)) {
			XpProgression.awardWorkstationOnce(serverPlayer, Blocks.RESPAWN_ANCHOR);
		}
	}
}
