package com.viclev.wildways.mixin;

import com.viclev.wildways.EyeTransformations;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEyeTransformationMixin {
	@Unique
	private int wildways$eyeWarmup;

	@Inject(method = "tick", at = @At("TAIL"))
	private void wildways$tickEyeTransformation(CallbackInfo ci) {
		ItemEntity item = (ItemEntity) (Object) this;
		if (item.level() instanceof ServerLevel level && item.isAlive()) {
			this.wildways$eyeWarmup = EyeTransformations.tickItem(level, item, this.wildways$eyeWarmup);
		}
	}
}
