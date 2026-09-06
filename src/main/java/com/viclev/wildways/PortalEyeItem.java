package com.viclev.wildways;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class PortalEyeItem extends Item {
	private final boolean requiresEnchantment;

	public PortalEyeItem(Properties properties, boolean requiresEnchantment) {
		super(properties);
		this.requiresEnchantment = requiresEnchantment;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		ItemStack stack = context.getItemInHand();
		if (this.requiresEnchantment && !stack.isEnchanted()) {
			return PortalEyeManager.rejectUnenchanted(context);
		}

		return PortalEyeManager.tryPlace(context);
	}
}
