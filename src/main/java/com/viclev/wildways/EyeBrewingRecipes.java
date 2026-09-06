package com.viclev.wildways;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class EyeBrewingRecipes {
	private EyeBrewingRecipes() {
	}

	public static boolean isInput(ItemStack stack) {
		return stack.is(Items.ENDER_EYE) || stack.is(ModItems.AWKWARD_EYE) || stack.is(ModItems.THICK_EYE);
	}

	public static boolean hasMix(ItemStack input, ItemStack ingredient) {
		return getOutput(input, ingredient) != null;
	}

	public static ItemStack mix(ItemStack input, ItemStack ingredient) {
		Item output = getOutput(input, ingredient);
		return output == null ? input : input.transmuteCopy(output);
	}

	private static Item getOutput(ItemStack input, ItemStack ingredient) {
		if (input.is(Items.ENDER_EYE) && ingredient.is(Items.NETHER_WART)) {
			return ModItems.AWKWARD_EYE;
		}
		if (input.is(ModItems.AWKWARD_EYE) && ingredient.is(Items.PHANTOM_MEMBRANE)) {
			return ModItems.THICK_EYE;
		}
		if (input.is(ModItems.THICK_EYE) && ingredient.is(Items.GHAST_TEAR)) {
			return ModItems.EYE_OF_BREWING;
		}
		return null;
	}
}
