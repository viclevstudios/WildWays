package com.viclev.wildways.mixin;

import com.viclev.wildways.ProgressionItems;
import com.viclev.wildways.ProgressionMaterials;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Apply equipment traits to the result of smithing upgrades. */
@Mixin(SmithingTransformRecipe.class)
public abstract class AmethystHeritageMixin {
	@Inject(method = "assemble(Lnet/minecraft/world/item/crafting/SmithingRecipeInput;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"))
	private void wildways$carryMaterialTraits(SmithingRecipeInput input, CallbackInfoReturnable<ItemStack> cir) {
		ItemStack output = cir.getReturnValue();
		if (output.isEmpty()) return;

		Item base = input.base().getItem();
		if (ProgressionItems.isAmethystGear(base)) {
			CustomData.update(DataComponents.CUSTOM_DATA, output, tag -> tag.putBoolean("wildways_amethyst_heritage", true));
		}
		if (input.template().is(ProgressionItems.REFINEMENT_DIAMOND)) {
			if (ProgressionItems.QUARTZ_SET.contains(base) || ProgressionItems.QUARTZ_TOOLS.contains(base)) {
				CustomData.update(DataComponents.CUSTOM_DATA, output, tag -> tag.putBoolean("wildways_quartz_heritage", true));
			} else if (ProgressionItems.GOLD_SET.contains(base) || ProgressionItems.GOLD_TOOLS.contains(base) || base == Items.GOLDEN_SPEAR) {
				CustomData.update(DataComponents.CUSTOM_DATA, output, tag -> tag.putBoolean("wildways_gold_heritage", true));
			}
		}
		if (ProgressionItems.hasQuartzHeritage(output)) {
			// Smithing copies the base's component patch. Recalculate from the new tier's default.
			Integer durability = output.getPrototype().get(DataComponents.MAX_DAMAGE);
			if (durability != null) {
				output.set(DataComponents.MAX_DAMAGE, Math.round(durability * ProgressionMaterials.QUARTZ_HERITAGE_DURABILITY_MULTIPLIER));
			}
		}
	}
}
