package com.viclev.wildways;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

public final class ElytraDurability {
	private ElytraDurability() {
	}

	public static boolean isAtLastPoint(ItemStack stack) {
		return stack.has(DataComponents.GLIDER) && stack.isDamageableItem()
			&& stack.getMaxDamage() - stack.getDamageValue() <= 1;
	}
}
