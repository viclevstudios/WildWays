package com.viclev.wildways.mixin;

import com.viclev.wildways.XpProgression;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractCauldronBlock.class)
public abstract class CauldronWorkstationXpMixin {
	@Inject(method = "useItemOn", at = @At("TAIL"))
	private void wildways$rewardCauldronUse(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
		InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
		if (player instanceof ServerPlayer serverPlayer && !state.equals(level.getBlockState(pos))) {
			XpProgression.awardWorkstationOnce(serverPlayer, Blocks.CAULDRON);
		}
	}
}
