package com.viclev.wildways.mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.LingeringPotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LingeringPotionItem.class)
public class LingeringPotionItemStackSizeMixin {
	@ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
	private static Item.Properties wildways$makeLingeringPotionsStackToEight(Item.Properties properties) {
		return properties.stacksTo(8);
	}
}
