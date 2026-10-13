package com.viclev.wildways.mixin;

import com.viclev.wildways.XpProgression;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TamableAnimal.class)
public abstract class TamableAnimalXpMixin {
	@Inject(method = "tame", at = @At("TAIL"))
	private void wildways$rewardFirstTaming(Player player, CallbackInfo ci) {
		XpProgression.awardFirstTame((TamableAnimal)(Object)this, player);
	}
}
