package com.viclev.wildways.mixin;

import com.viclev.wildways.ProgressionItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class AmethystArmorWebMixin {
	@Inject(method = "makeStuckInBlock", at = @At("HEAD"), cancellable = true)
	private void wildways$ignoreCobwebSlowdown(BlockState blockState, Vec3 speedMultiplier, CallbackInfo ci) {
		Entity entity = (Entity) (Object) this;
		if (blockState.is(Blocks.COBWEB) && entity instanceof LivingEntity living && isWearingAmethystArmor(living)) {
			entity.resetFallDistance();
			ci.cancel();
		}
	}

	private static boolean isWearingAmethystArmor(LivingEntity entity) {
		return ProgressionItems.isAmethystArmor(entity.getItemBySlot(EquipmentSlot.HEAD).getItem())
			|| ProgressionItems.isAmethystArmor(entity.getItemBySlot(EquipmentSlot.CHEST).getItem())
			|| ProgressionItems.isAmethystArmor(entity.getItemBySlot(EquipmentSlot.LEGS).getItem())
			|| ProgressionItems.isAmethystArmor(entity.getItemBySlot(EquipmentSlot.FEET).getItem());
	}
}
