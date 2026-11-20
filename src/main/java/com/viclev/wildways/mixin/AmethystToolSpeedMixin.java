package com.viclev.wildways.mixin;

import com.viclev.wildways.ProgressionItems;
import com.viclev.wildways.ProgressionMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class AmethystToolSpeedMixin {
	@Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
	private void wildways$adjustMiningSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
		ItemStack stack = (ItemStack) (Object) this;
		float speed = cir.getReturnValue();
		if (speed <= 1.0F) {
			if (ProgressionItems.isAmethystTool(stack.getItem()) || ProgressionItems.hasAmethystHeritage(stack)) {
				cir.setReturnValue(1.5F);
			}
		} else {
			int penalties = ProgressionItems.strengthPenalties(stack);
			if (penalties > 0) {
				cir.setReturnValue(Math.max(1.0F, speed - penalties * ProgressionMaterials.MINING_SPEED_PENALTY));
			}
		}
	}
}
