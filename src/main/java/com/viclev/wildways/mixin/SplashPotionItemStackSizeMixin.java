package com.viclev.wildways.mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SplashPotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SplashPotionItem.class)
public class SplashPotionItemStackSizeMixin {
	@ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
	private static Item.Properties wildways$makeSplashPotionsStackToEight(Item.Properties properties) {
		return properties.stacksTo(8);
	}
}
