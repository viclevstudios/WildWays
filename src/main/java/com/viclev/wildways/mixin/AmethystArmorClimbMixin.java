package com.viclev.wildways.mixin;

import com.viclev.wildways.ProgressionItems;
import com.viclev.wildways.ProgressionMaterials;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class AmethystArmorClimbMixin {
	@Inject(method = "travelInAir", at = @At("TAIL"))
	private void wildways$climbFaster(Vec3 input, CallbackInfo ci) {
		LivingEntity entity = (LivingEntity) (Object) this;
		if (!entity.onClimbable() || !isWearingAmethystArmor(entity)) {
			return;
		}

		Vec3 movement = entity.getDeltaMovement();
		if (movement.y > 0.0D) {
			entity.setDeltaMovement(movement.x, movement.y * ProgressionMaterials.AMETHYST_CLIMB_SPEED_MULTIPLIER, movement.z);
		}
	}

	private static boolean isWearingAmethystArmor(LivingEntity entity) {
		return ProgressionItems.isAmethystArmor(entity.getItemBySlot(EquipmentSlot.HEAD))
			|| ProgressionItems.isAmethystArmor(entity.getItemBySlot(EquipmentSlot.CHEST))
			|| ProgressionItems.isAmethystArmor(entity.getItemBySlot(EquipmentSlot.LEGS))
			|| ProgressionItems.isAmethystArmor(entity.getItemBySlot(EquipmentSlot.FEET));
	}
}
