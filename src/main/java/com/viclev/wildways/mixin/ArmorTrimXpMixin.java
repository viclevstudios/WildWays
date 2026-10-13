package com.viclev.wildways.mixin;

import com.viclev.wildways.XpProgression;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingMenu.class)
public abstract class ArmorTrimXpMixin {
	@Inject(method = "onTake", at = @At("TAIL"))
	private void wildways$rewardArmorTrim(Player player, ItemStack result, CallbackInfo ci) {
		XpProgression.awardArmorTrim(player, result);
	}
}
