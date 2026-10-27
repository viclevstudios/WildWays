package com.viclev.wildways.mixin;

import com.viclev.wildways.XpProgression;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LecternBlock.class)
public abstract class LecternBlockWorkstationXpMixin {
	@Inject(method = "useItemOn", at = @At("TAIL"))
	private void wildways$rewardPlacingLecternBook(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
		InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
		if (player instanceof ServerPlayer serverPlayer && cir.getReturnValue().consumesAction()
			&& !state.getValue(LecternBlock.HAS_BOOK)
			&& level.getBlockState(pos).getValue(LecternBlock.HAS_BOOK)) {
			XpProgression.awardWorkstationOnce(serverPlayer, Blocks.LECTERN);
		}
	}
}
