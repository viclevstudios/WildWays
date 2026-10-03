package com.viclev.wildways.mixin;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Recipe books, including old saves, show the enchantment type without a level. */
@Mixin(ItemEnchantments.class)
public class RecipeBookTooltipMixin {
	@Redirect(method = "addToTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;getFullname(Lnet/minecraft/core/Holder;I)Lnet/minecraft/network/chat/Component;"))
	private Component wildways$describeRecipe(Holder<Enchantment> enchantment, int level, Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {
		if (components.get(DataComponents.STORED_ENCHANTMENTS) != null) {
			return enchantment.value().description().copy().withStyle(enchantment.is(EnchantmentTags.CURSE) ? ChatFormatting.RED : ChatFormatting.GRAY);
		}
		return Enchantment.getFullname(enchantment, level);
	}
}
