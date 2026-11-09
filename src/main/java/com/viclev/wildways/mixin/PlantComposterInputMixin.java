package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.viclev.wildways.PlantComposting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Compostable;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.level.block.ComposterBlock$InputContainer")
abstract class PlantComposterInputMixin {
	@Shadow @Final private BlockState state;

	@ModifyReturnValue(method = "canPlaceItemThroughFace", at = @At("RETURN"))
	private boolean wildways$rejectMixedHopperInput(boolean allowed, int slot, ItemStack stack, Direction direction) {
		return allowed && PlantComposting.accepts(this.state, stack);
	}

	@WrapOperation(method = "setChanged", at = @At(value = "INVOKE", target =
		"Lnet/minecraft/world/level/block/ComposterBlock;addLayer(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/component/Compostable;)Lnet/minecraft/world/level/block/state/BlockState;"))
	private BlockState wildways$recordHopperBatch(Entity entity, BlockState state, ServerLevel level, BlockPos pos,
		Compostable compostable, Operation<BlockState> original, @Local ItemStack stack) {
		BlockState batch = PlantComposting.batchFor(state, stack);
		BlockState result = original.call(entity, batch, level, pos, compostable);
		return result == batch ? state : result;
	}
}
