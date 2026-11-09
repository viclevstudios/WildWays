package com.viclev.wildways.mixin;

import com.viclev.wildways.PlantComposting;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "net.minecraft.world.level.block.ComposterBlock$OutputContainer")
abstract class PlantComposterOutputMixin {
	@Shadow @Final private BlockState state;

	@ModifyArg(method = "canTakeItemThroughFace", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z"))
	private Object wildways$allowCompostOutput(Object expected) {
		return PlantComposting.output(this.state);
	}
}
