package com.viclev.wildways;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public final class VolcaniteMaterials {
	public static final TagKey<Item> REPAIR_ITEMS = TagKey.create(Registries.ITEM, Wildways.id("volcanite_materials"));
	private static final ToolMaterial DIAMOND_TOOL = ToolMaterial.DIAMOND;
	private static final ArmorMaterial DIAMOND_ARMOR = ArmorMaterials.DIAMOND;
	private static final ResourceKey<EquipmentAsset> VOLCANITE_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, Wildways.id("volcanite"));

	public static final ToolMaterial TOOL = new ToolMaterial(
		DIAMOND_TOOL.incorrectBlocksForDrops(), DIAMOND_TOOL.durability(), DIAMOND_TOOL.speed(),
		DIAMOND_TOOL.attackDamageBonus(), DIAMOND_TOOL.enchantmentValue(), REPAIR_ITEMS
	);
	public static final ArmorMaterial ARMOR = new ArmorMaterial(
		DIAMOND_ARMOR.durability(), DIAMOND_ARMOR.defense(), DIAMOND_ARMOR.enchantmentValue(),
		DIAMOND_ARMOR.equipSound(), DIAMOND_ARMOR.toughness(), DIAMOND_ARMOR.knockbackResistance(),
		REPAIR_ITEMS, VOLCANITE_ASSET
	);

	private VolcaniteMaterials() {
	}
}
