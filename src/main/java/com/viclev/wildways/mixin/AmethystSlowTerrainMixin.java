package com.viclev.wildways.mixin;

import com.viclev.wildways.ProgressionItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class AmethystSlowTerrainMixin {
	@Inject(method = "getBlockSpeedFactor", at = @At("RETURN"), cancellable = true)
	private void wildways$ignoreSlowGround(CallbackInfoReturnable<Float> cir) {
		Entity entity = (Entity) (Object) this;
		if (entity instanceof LivingEntity living && (ProgressionItems.isAmethystArmor(living.getItemBySlot(EquipmentSlot.HEAD))
			|| ProgressionItems.isAmethystArmor(living.getItemBySlot(EquipmentSlot.CHEST))
			|| ProgressionItems.isAmethystArmor(living.getItemBySlot(EquipmentSlot.LEGS))
			|| ProgressionItems.isAmethystArmor(living.getItemBySlot(EquipmentSlot.FEET)))) {
			cir.setReturnValue(1.0F);
		}
	}
}
