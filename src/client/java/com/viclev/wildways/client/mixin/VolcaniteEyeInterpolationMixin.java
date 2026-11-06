package com.viclev.wildways.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.viclev.wildways.EyeTransformations;
import net.minecraft.core.PositionAndRotation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LinearInterpolationHandler;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PositionPath;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public abstract class VolcaniteEyeInterpolationMixin extends Entity {
	protected VolcaniteEyeInterpolationMixin(EntityType<?> type, Level level) {
		super(type, level);
	}

	@Override
	protected InterpolationHandler createInterpolationHandler() {
		ItemEntity item = (ItemEntity)(Object)this;
		InterpolationHandler rise = LinearInterpolationHandler.create(item, 2);
		return new InterpolationHandler.NoOpInterpolationHandler() {
			@Override
			public boolean interpolateTo(@Nullable PositionPath position, float yRot, float xRot, boolean hasRotation) {
				return (rise.hasActiveInterpolation() || EyeTransformations.isVolcaniteRising(item))
					&& rise.interpolateTo(position, yRot, xRot, hasRotation);
			}

			@Override
			public @Nullable PositionAndRotation target() {
				return rise.target();
			}

			@Override
			public boolean hasActiveInterpolation() {
				return rise.hasActiveInterpolation();
			}

			@Override
			public void interpolate() {
				rise.interpolate();
			}

			@Override
			public void cancel() {
				rise.cancel();
			}
		};
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/world/entity/item/ItemEntity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
	private void wildways$skipPredictedMovementDuringRise(ItemEntity eye, MoverType mover, Vec3 movement, Operation<Void> original) {
		if (!this.level().isClientSide() || (!this.isInterpolating() && !EyeTransformations.isVolcaniteRising(eye))) {
			original.call(eye, mover, movement);
		}
	}
}
