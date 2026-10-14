package com.viclev.wildways.mixin;

import com.viclev.wildways.ProgressionItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Preserve Amethyst's special movement and tool traits through later smithing upgrades. */
@Mixin(SmithingTransformRecipe.class)
public abstract class AmethystHeritageMixin {
	@Inject(method = "assemble(Lnet/minecraft/world/item/crafting/SmithingRecipeInput;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"), cancellable = true)
	private void wildways$carryAmethystTraits(SmithingRecipeInput input, CallbackInfoReturnable<ItemStack> cir) {
		if (ProgressionItems.isAmethystGear(input.base().getItem())) {
			ItemStack output = cir.getReturnValue();
			CustomData.update(DataComponents.CUSTOM_DATA, output, tag -> tag.putBoolean("wildways_amethyst_heritage", true));
			cir.setReturnValue(output);
		}
	}
}
