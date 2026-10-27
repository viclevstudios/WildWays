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
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ComposterBlock.class)
public abstract class ComposterWorkstationXpMixin {
	@Unique private boolean wildways$validCompostingInput;

	@Inject(method = "useItemOn", at = @At("HEAD"))
	private void wildways$recordComposterInput(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
		InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
		this.wildways$validCompostingInput = state.getValue(ComposterBlock.LEVEL) < 7
			&& ComposterBlock.COMPOSTABLES.containsKey(stack.getItem());
	}

	@Inject(method = "useItemOn", at = @At("TAIL"))
	private void wildways$rewardComposterUse(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
		InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
		if (player instanceof ServerPlayer serverPlayer && cir.getReturnValue().consumesAction()
			&& this.wildways$validCompostingInput) {
			XpProgression.awardWorkstationOnce(serverPlayer, Blocks.COMPOSTER);
		}
		this.wildways$validCompostingInput = false;
	}

	@Inject(method = "useWithoutItem", at = @At("TAIL"))
	private void wildways$rewardComposterHarvest(BlockState state, Level level, BlockPos pos, Player player,
		BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
		if (player instanceof ServerPlayer serverPlayer && cir.getReturnValue().consumesAction()
			&& state.getValue(ComposterBlock.LEVEL) == 8) {
			XpProgression.awardWorkstationOnce(serverPlayer, Blocks.COMPOSTER);
		}
	}
}
