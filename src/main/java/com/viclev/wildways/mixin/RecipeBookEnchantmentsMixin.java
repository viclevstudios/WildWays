package com.viclev.wildways.mixin;

import com.viclev.wildways.EnchantingRules;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.function.Consumer;

@Mixin(EnchantmentHelper.class)
public class RecipeBookEnchantmentsMixin {
	@ModifyVariable(method = "setEnchantments", at = @At("HEAD"), argsOnly = true)
	private static ItemEnchantments wildways$removeBookLevels(ItemEnchantments enchantments, ItemStack stack, ItemEnchantments original) {
		return stack.is(Items.ENCHANTED_BOOK) ? EnchantingRules.normalizeBook(enchantments) : enchantments;
	}

	@Inject(method = "updateEnchantments", at = @At("RETURN"), cancellable = true)
	private static void wildways$normalizeGeneratedBook(ItemStack stack, Consumer<ItemEnchantments.Mutable> consumer, CallbackInfoReturnable<ItemEnchantments> cir) {
		if (stack.is(Items.ENCHANTED_BOOK)) {
			ItemEnchantments normalized = EnchantingRules.normalizeBook(cir.getReturnValue());
			stack.set(DataComponents.STORED_ENCHANTMENTS, normalized);
			cir.setReturnValue(normalized);
		}
	}
}
