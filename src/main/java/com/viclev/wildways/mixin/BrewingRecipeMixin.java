package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.viclev.wildways.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BrewingInput;
import net.minecraft.world.item.crafting.BrewingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BrewingRecipe.class)
public class BrewingRecipeMixin {
	@ModifyReturnValue(
		method = "assemble(Lnet/minecraft/world/item/crafting/BrewingInput;)Lnet/minecraft/world/item/ItemStack;",
		at = @At("RETURN")
	)
	private ItemStack wildways$preserveEyeComponents(ItemStack result, BrewingInput input) {
		if (result.is(ModItems.AWKWARD_EYE) || result.is(ModItems.THICK_EYE) || result.is(ModItems.EYE_OF_BREWING)) {
			ItemStack eye = input.input().transmuteCopy(result.getItem());
			eye.applyComponents(result.getComponentsPatch());
			return eye;
		}
		return result;
	}
}
