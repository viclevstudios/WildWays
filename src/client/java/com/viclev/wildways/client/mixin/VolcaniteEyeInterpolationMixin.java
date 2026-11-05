package com.viclev.wildways.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.viclev.wildways.EyeTransformations;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class VolcaniteEyeInterpolationMixin extends Entity {
	@Unique
	private @Nullable InterpolationHandler wildways$riseInterpolation;

	protected VolcaniteEyeInterpolationMixin(EntityType<?> type, Level level) {
		super(type, level);
	}

	@Override
	public @Nullable InterpolationHandler getInterpolation() {
		if (this.level().isClientSide()) {
			if (this.wildways$riseInterpolation != null && this.wildways$riseInterpolation.hasActiveInterpolation()) {
				return this.wildways$riseInterpolation;
			}
			if (EyeTransformations.isVolcaniteRising((ItemEntity)(Object)this)) {
				if (this.wildways$riseInterpolation == null) {
					this.wildways$riseInterpolation = new InterpolationHandler(this, 2);
				}
				return this.wildways$riseInterpolation;
			}
		}
		return super.getInterpolation();
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/world/entity/item/ItemEntity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
	private void wildways$skipPredictedMovementDuringRise(ItemEntity eye, MoverType mover, Vec3 movement, Operation<Void> original) {
		if (this.getInterpolation() == null) {
			original.call(eye, mover, movement);
		}
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void wildways$interpolateRise(CallbackInfo ci) {
		if (this.wildways$riseInterpolation != null && this.wildways$riseInterpolation.hasActiveInterpolation()) {
			this.wildways$riseInterpolation.interpolate();
		}
	}
}
