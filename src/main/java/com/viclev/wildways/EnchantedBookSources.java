package com.viclev.wildways;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

public final class EnchantedBookSources {
	public static final TagKey<Enchantment> GENERAL_BOOKS =
		TagKey.create(Registries.ENCHANTMENT, Wildways.id("general_books"));

	private EnchantedBookSources() {
	}

	public static ItemStack book(ServerLevel level, ResourceKey<Enchantment> enchantment) {
		return book(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment));
	}

	public static ItemStack randomBook(ServerLevel level, RandomSource random) {
		Holder<Enchantment> enchantment = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
			.getRandomElementOf(GENERAL_BOOKS, random).orElseThrow();
		return book(enchantment);
	}

	private static ItemStack book(Holder<Enchantment> enchantment) {
		ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
		stack.enchant(enchantment, 1);
		return stack;
	}
}
