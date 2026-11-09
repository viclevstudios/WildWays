package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.ToIntFunction;

@Mixin(BlockBehaviour.BlockStateBase.class)
abstract class PlantLightMixin {
	@WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/function/ToIntFunction;applyAsInt(Ljava/lang/Object;)I"))
	private int wildways$lightTorchflowers(ToIntFunction<?> light, Object state, Operation<Integer> original, Block owner) {
		return switch (owner.getDescriptionId()) {
			case "block.minecraft.torchflower", "block.minecraft.potted_torchflower" -> 9;
			default -> original.call(light, state);
		};
	}
}
