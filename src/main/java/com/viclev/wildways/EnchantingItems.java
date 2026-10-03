package com.viclev.wildways;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class EnchantingItems {
	public static final List<Item> RUNES = registerRunes();
	public static final Item CATALYST = registerCatalyst();

	private EnchantingItems() {
	}

	private static List<Item> registerRunes() {
		List<Item> items = new ArrayList<>();
		for (int tier = 1; tier <= 5; tier++) {
			ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Wildways.id("rune_" + tier));
			Item.Properties properties = new Item.Properties().setId(key);
			items.add(Registry.register(BuiltInRegistries.ITEM, key, new Item(properties)));
		}
		return List.copyOf(items);
	}

	private static Item registerCatalyst() {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Wildways.id("enchanting_catalyst"));
		return Registry.register(BuiltInRegistries.ITEM, key, new Item(new Item.Properties().setId(key).durability(4)));
	}

	public static int runeTier(ItemStack stack) {
		return RUNES.indexOf(stack.getItem()) + 1;
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> {
			RUNES.forEach(output::accept);
			output.accept(CATALYST);
		});
	}
}
