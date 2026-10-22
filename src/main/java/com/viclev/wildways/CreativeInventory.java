package com.viclev.wildways;

import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Keeps equipment grouped by material in both vanilla equipment tabs. */
public final class CreativeInventory {
	private CreativeInventory() {
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> {
			output.insertAfter(Items.IRON_HOE,
				ProgressionItems.QUARTZ_SHOVEL, ProgressionItems.QUARTZ_PICKAXE, ProgressionItems.QUARTZ_AXE, ProgressionItems.QUARTZ_HOE,
				ProgressionItems.AMETHYST_SHOVEL, ProgressionItems.AMETHYST_PICKAXE, ProgressionItems.AMETHYST_AXE, ProgressionItems.AMETHYST_HOE);
			output.insertAfter(Items.DIAMOND_HOE, ModItems.VOLCANITE_SHOVEL, ModItems.VOLCANITE_PICKAXE, ModItems.VOLCANITE_AXE, ModItems.VOLCANITE_HOE);
		});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(CreativeInventory::combatByMaterial);
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output ->
			output.insertBefore(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, ProgressionItems.REFINEMENTS.toArray(Item[]::new)));
	}

	private static void combatByMaterial(FabricCreativeModeTabOutput output) {
		// Each material has its sword, spear, axe, then its complete armor set where available.
		List<Item> equipment = List.of(
			Items.WOODEN_SWORD, Items.WOODEN_SPEAR, Items.WOODEN_AXE,
			Items.STONE_SWORD, Items.STONE_SPEAR, Items.STONE_AXE,
			Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS,
			Items.COPPER_SWORD, Items.COPPER_SPEAR, Items.COPPER_AXE,
			Items.COPPER_HELMET, Items.COPPER_CHESTPLATE, Items.COPPER_LEGGINGS, Items.COPPER_BOOTS,
			Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS,
			Items.IRON_SWORD, Items.IRON_SPEAR, Items.IRON_AXE,
			Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS,
			ProgressionItems.QUARTZ_SWORD, ProgressionItems.QUARTZ_SPEAR, ProgressionItems.QUARTZ_AXE,
			ProgressionItems.QUARTZ_HELMET, ProgressionItems.QUARTZ_CHESTPLATE, ProgressionItems.QUARTZ_LEGGINGS, ProgressionItems.QUARTZ_BOOTS,
			ProgressionItems.AMETHYST_SWORD, ProgressionItems.AMETHYST_SPEAR, ProgressionItems.AMETHYST_AXE,
			ProgressionItems.AMETHYST_HELMET, ProgressionItems.AMETHYST_CHESTPLATE, ProgressionItems.AMETHYST_LEGGINGS, ProgressionItems.AMETHYST_BOOTS,
			Items.GOLDEN_SWORD, Items.GOLDEN_SPEAR, Items.GOLDEN_AXE,
			Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS,
			Items.DIAMOND_SWORD, Items.DIAMOND_SPEAR, Items.DIAMOND_AXE,
			Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS,
			ModItems.VOLCANITE_SWORD, ModItems.VOLCANITE_SPEAR, ModItems.VOLCANITE_AXE,
			ModItems.VOLCANITE_HELMET, ModItems.VOLCANITE_CHESTPLATE, ModItems.VOLCANITE_LEGGINGS, ModItems.VOLCANITE_BOOTS,
			Items.NETHERITE_SWORD, Items.NETHERITE_SPEAR, Items.NETHERITE_AXE,
			Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);
		Set<Item> grouped = Set.copyOf(equipment);
		output.getDisplayStacks().removeIf(stack -> grouped.contains(stack.getItem()));
		output.getSearchTabStacks().removeIf(stack -> grouped.contains(stack.getItem()));
		output.insertBefore(Items.TRIDENT, equipment.toArray(Item[]::new));
	}
}
