package com.viclev.wildways.client.mixin;

import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilScreen.class)
public class AnvilCostDisplayMixin {
	@ModifyConstant(method = "extractLabels", constant = @Constant(intValue = 40))
	private int wildways$showCostsAboveForty(int limit) {
		return Integer.MAX_VALUE;
	}
}
