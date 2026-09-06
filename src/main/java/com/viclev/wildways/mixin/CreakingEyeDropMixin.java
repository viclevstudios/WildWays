package com.viclev.wildways.mixin;

import com.viclev.wildways.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Creaking.class)
public class CreakingEyeDropMixin {
	@Unique
	private boolean wildways$droppedEye;

	@Inject(method = "hurtServer", at = @At("RETURN"))
	private void wildways$tryDropEye(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
		if (!this.wildways$droppedEye && cir.getReturnValue() && level.getRandom().nextFloat() < 0.05F) {
			this.wildways$droppedEye = true;
			((Creaking)(Object)this).spawnAtLocation(level, ModItems.EYE_OF_THE_CREAKING);
		}
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void wildways$saveEyeDrop(ValueOutput output, CallbackInfo ci) {
		output.putBoolean("WildwaysDroppedEye", this.wildways$droppedEye);
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void wildways$loadEyeDrop(ValueInput input, CallbackInfo ci) {
		this.wildways$droppedEye = input.getBooleanOr("WildwaysDroppedEye", false);
	}
}
