package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.viclev.wildways.PlantComposting;
import com.viclev.wildways.PlantGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Compostable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ComposterBlock.class)
abstract class PlantComposterMixin {
	@Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
	private void wildways$rejectMixedInput(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
		InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
		if (state.getValue(ComposterBlock.LEVEL) < 7 && stack.has(DataComponents.COMPOSTABLE)
			&& !PlantComposting.accepts(state, stack)) {
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}

	@Inject(method = "insertItem", at = @At("HEAD"), cancellable = true)
	private static void wildways$rejectMixedInsertion(Entity entity, BlockState state, ServerLevel level, ItemStack stack,
		BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
		if (!PlantComposting.accepts(state, stack)) cir.setReturnValue(state);
	}

	@WrapOperation(method = "useItemOn", at = @At(value = "INVOKE", target =
		"Lnet/minecraft/world/level/block/ComposterBlock;addLayer(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/component/Compostable;)Lnet/minecraft/world/level/block/state/BlockState;"))
	private BlockState wildways$recordPlayerBatch(Entity entity, BlockState state, ServerLevel level, BlockPos pos,
		Compostable compostable, Operation<BlockState> original, @Local(argsOnly = true) ItemStack stack) {
		BlockState batch = PlantComposting.batchFor(state, stack);
		BlockState result = original.call(entity, batch, level, pos, compostable);
		return result == batch ? state : result;
	}

	@WrapOperation(method = "insertItem", at = @At(value = "INVOKE", target =
		"Lnet/minecraft/world/level/block/ComposterBlock;addLayer(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/component/Compostable;)Lnet/minecraft/world/level/block/state/BlockState;"))
	private static BlockState wildways$recordEntityBatch(Entity entity, BlockState state, ServerLevel level, BlockPos pos,
		Compostable compostable, Operation<BlockState> original, @Local(argsOnly = true) ItemStack stack) {
		BlockState batch = PlantComposting.batchFor(state, stack);
		BlockState result = original.call(entity, batch, level, pos, compostable);
		return result == batch ? state : result;
	}

	@ModifyExpressionValue(method = {"extractProduce", "getContainer"}, at = @At(value = "NEW", target = "net/minecraft/world/item/ItemStack"))
	private static ItemStack wildways$selectCompost(ItemStack original, @Local(argsOnly = true) BlockState state) {
		return PlantComposting.isSnifferBatch(state) ? new ItemStack(PlantComposting.output(state)) : original;
	}

	@ModifyVariable(method = "empty", at = @At("HEAD"), argsOnly = true)
	private static BlockState wildways$clearBatch(BlockState state) {
		return state.setValue(PlantGrowth.SNIFFER_COMPOST, 0);
	}
}
