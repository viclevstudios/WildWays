package com.viclev.wildways;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class EnchantingRules {
	public static final TagKey<Enchantment> LEVEL_FIVE = TagKey.create(Registries.ENCHANTMENT, Wildways.id("requires_level_five_rune"));

	private EnchantingRules() {
	}

	public static List<Holder<Enchantment>> recipes(ItemStack book) {
		if (!book.is(Items.ENCHANTED_BOOK)) {
			return List.of();
		}
		return book.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).keySet().stream()
			.sorted(Comparator.comparing(holder -> holder.unwrapKey().map(key -> key.identifier().toString()).orElse("")))
			.toList();
	}

	public static ItemEnchantments normalizeBook(ItemEnchantments enchantments) {
		ItemEnchantments.Mutable normalized = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
		enchantments.keySet().forEach(enchantment -> normalized.set(enchantment, 1));
		return normalized.toImmutable();
	}

	public static boolean isPrecious(Holder<Enchantment> enchantment) {
		return enchantment.is(LEVEL_FIVE);
	}

	public static int requiredShelves(int runeTier) {
		return (runeTier - 1) * 5;
	}

	public static int enchantmentLevel(Holder<Enchantment> enchantment, int runeTier) {
		if (isPrecious(enchantment)) {
			return runeTier == 5 ? 1 : 0;
		}
		return runeTier <= enchantment.value().getMaxLevel() ? runeTier : 0;
	}

	public static boolean compatible(ItemStack target, Holder<Enchantment> enchantment, int level) {
		if (level <= 0 || !enchantment.value().canEnchant(target)) {
			return false;
		}
		ItemEnchantments current = EnchantmentHelper.getEnchantmentsForCrafting(target);
		return current.getLevel(enchantment) < level && current.keySet().stream()
			.allMatch(other -> other.equals(enchantment) || Enchantment.areCompatible(enchantment, other));
	}

	public static int experienceCost(int tier, boolean catalyst) {
		return catalyst ? (tier + 1) / 2 : tier;
	}

	public static boolean hasMending(ItemStack target) {
		return target.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).keySet().stream()
			.anyMatch(enchantment -> enchantment.is(Enchantments.MENDING));
	}

	public static int repairCost(ItemStack target) {
		return Math.max(1, target.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).entrySet().stream()
			.mapToInt(entry -> entry.getIntValue()).max().orElse(1));
	}

}
