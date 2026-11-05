package com.viclev.wildways.mixin;

import com.viclev.wildways.FishingBait;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class FishingFoodStackMixin {
	@Inject(method = "<init>(Lnet/minecraft/core/Holder;ILnet/minecraft/core/component/DataComponentPatch;)V", at = @At("RETURN"))
	private void wildways$updateSavedFish(Holder<Item> item, int count, DataComponentPatch patch, CallbackInfo ci) {
		if (!patch.isEmpty() && patch.get(item.components(), FishingBait.RICH_FISH) != null) {
			FishingBait.enrichFish((ItemStack) (Object) this);
		}
	}
}
