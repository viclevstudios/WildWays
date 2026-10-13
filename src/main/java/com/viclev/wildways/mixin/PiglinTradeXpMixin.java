package com.viclev.wildways.mixin;

import com.viclev.wildways.XpProgression;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PiglinAi.class)
public abstract class PiglinTradeXpMixin {
	@Inject(method = "getBarterResponseItems", at = @At("TAIL"))
	private static void wildways$rewardFirstPiglinTrade(Piglin piglin, CallbackInfoReturnable<List<ItemStack>> cir) {
		if (!cir.getReturnValue().isEmpty()) {
			XpProgression.awardFirstPiglinTrade(piglin);
		}
	}
}
