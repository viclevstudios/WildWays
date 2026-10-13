package com.viclev.wildways;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.block.Block;

import java.util.Map;

/** Central starting values for the added equipment tiers; adjust these after playtesting. */
public final class ProgressionMaterials {
	public static final int QUARTZ_TOOL_DURABILITY = 600;
	public static final int AMETHYST_TOOL_DURABILITY = 250;
	public static final int GOLD_TOOL_DURABILITY = 400;
	public static final int QUARTZ_ARMOR_DURABILITY = 25;
	public static final int AMETHYST_ARMOR_DURABILITY = 19;
	public static final int GOLD_ARMOR_DURABILITY = 19;
	public static final int VOLCANITE_TOOL_DURABILITY = 1400;
	public static final int VOLCANITE_ARMOR_DURABILITY = 28;
	public static final float QUARTZ_TOOL_SPEED = 6.0F;
	public static final float AMETHYST_TOOL_SPEED = 6.0F;
	public static final float GOLD_TOOL_SPEED = 7.0F;
	public static final float VOLCANITE_TOOL_SPEED = 7.5F;
	public static final float QUARTZ_ATTACK_DAMAGE_BONUS = 2.0F;
	public static final float AMETHYST_ATTACK_DAMAGE_BONUS = 2.0F;
	public static final float GOLD_ATTACK_DAMAGE_BONUS = 2.5F;
	public static final float VOLCANITE_ATTACK_DAMAGE_BONUS = 2.7F;
	public static final int QUARTZ_ENCHANTABILITY = 14;
	public static final int AMETHYST_ENCHANTABILITY = 16;
	public static final int GOLD_ENCHANTABILITY = 20;
	public static final int VOLCANITE_ENCHANTABILITY = 12;
	public static final float SWORD_ATTACK_DAMAGE = 3.0F;
	public static final float SWORD_ATTACK_SPEED = -2.4F;
	public static final float PICKAXE_ATTACK_DAMAGE = 1.0F;
	public static final float PICKAXE_ATTACK_SPEED = -2.8F;
	public static final float AXE_ATTACK_DAMAGE = 5.0F;
	public static final float AXE_ATTACK_SPEED = -3.0F;
	public static final float SHOVEL_ATTACK_DAMAGE = 1.5F;
	public static final float SHOVEL_ATTACK_SPEED = -3.0F;
	public static final float HOE_ATTACK_DAMAGE = -3.0F;
	public static final float HOE_ATTACK_SPEED = 0.0F;
	public static final float AMETHYST_ATTACK_SPEED_BONUS = 0.2F;
	public static final double AMETHYST_SPEED_PER_PIECE = 0.0025;
	public static final double AMETHYST_CLIMB_SPEED_MULTIPLIER = 1.25;
	public static final double VOLCANITE_RANGE_PER_PIECE = 0.25;

	public static final TagKey<Item> REPAIRS_QUARTZ = itemTag("repairs_quartz_equipment");
	public static final TagKey<Item> REPAIRS_AMETHYST = itemTag("repairs_amethyst_equipment");
	public static final TagKey<Item> REPAIRS_GOLD = itemTag("repairs_refined_gold_equipment");
	public static final TagKey<Item> REPAIRS_VOLCANITE = itemTag("volcanite_materials");
	public static final TagKey<Block> INCORRECT_QUARTZ = blockTag("incorrect_for_quartz_tool");
	public static final TagKey<Block> INCORRECT_AMETHYST = blockTag("incorrect_for_amethyst_tool");
	public static final TagKey<Block> INCORRECT_GOLD = blockTag("incorrect_for_refined_gold_tool");
	public static final TagKey<Block> INCORRECT_VOLCANITE = blockTag("incorrect_for_volcanite_tool");

	public static final ToolMaterial QUARTZ_TOOL = new ToolMaterial(INCORRECT_QUARTZ, QUARTZ_TOOL_DURABILITY, QUARTZ_TOOL_SPEED, QUARTZ_ATTACK_DAMAGE_BONUS, QUARTZ_ENCHANTABILITY, REPAIRS_QUARTZ);
	public static final ToolMaterial AMETHYST_TOOL = new ToolMaterial(INCORRECT_AMETHYST, AMETHYST_TOOL_DURABILITY, AMETHYST_TOOL_SPEED, AMETHYST_ATTACK_DAMAGE_BONUS, AMETHYST_ENCHANTABILITY, REPAIRS_AMETHYST);
	public static final ToolMaterial GOLD_TOOL = new ToolMaterial(INCORRECT_GOLD, GOLD_TOOL_DURABILITY, GOLD_TOOL_SPEED, GOLD_ATTACK_DAMAGE_BONUS, GOLD_ENCHANTABILITY, REPAIRS_GOLD);
	public static final ToolMaterial VOLCANITE_TOOL = new ToolMaterial(INCORRECT_VOLCANITE, VOLCANITE_TOOL_DURABILITY, VOLCANITE_TOOL_SPEED, VOLCANITE_ATTACK_DAMAGE_BONUS, VOLCANITE_ENCHANTABILITY, REPAIRS_VOLCANITE);

	public static final ResourceKey<EquipmentAsset> QUARTZ_ASSET = asset("quartz");
	public static final ResourceKey<EquipmentAsset> AMETHYST_ASSET = asset("amethyst");
	public static final ResourceKey<EquipmentAsset> GOLD_ASSET = asset("refined_gold");
	public static final ResourceKey<EquipmentAsset> VOLCANITE_ASSET = asset("volcanite");

	public static final ArmorMaterial QUARTZ_ARMOR = armor(QUARTZ_ARMOR_DURABILITY, 10, REPAIRS_QUARTZ, QUARTZ_ASSET, 2, 5, 6, 2);
	public static final ArmorMaterial AMETHYST_ARMOR = armor(AMETHYST_ARMOR_DURABILITY, 14, REPAIRS_AMETHYST, AMETHYST_ASSET, 2, 5, 6, 2);
	public static final ArmorMaterial GOLD_ARMOR = armor(GOLD_ARMOR_DURABILITY, 20, REPAIRS_GOLD, GOLD_ASSET, 2, 5, 7, 2);
	public static final ArmorMaterial VOLCANITE_ARMOR = armor(VOLCANITE_ARMOR_DURABILITY, 12, REPAIRS_VOLCANITE, VOLCANITE_ASSET, 2, 5, 7, 2);

	private ProgressionMaterials() {
	}

	private static ArmorMaterial armor(int durability, int enchantability, TagKey<Item> repairs, ResourceKey<EquipmentAsset> asset,
			int boots, int leggings, int chestplate, int helmet) {
		return new ArmorMaterial(durability, Map.of(
			ArmorType.BOOTS, boots,
			ArmorType.LEGGINGS, leggings,
			ArmorType.CHESTPLATE, chestplate,
			ArmorType.HELMET, helmet
		), enchantability, net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, repairs, asset);
	}

	private static TagKey<Item> itemTag(String path) {
		return TagKey.create(Registries.ITEM, Wildways.id(path));
	}

	private static TagKey<Block> blockTag(String path) {
		return TagKey.create(Registries.BLOCK, Wildways.id(path));
	}

	private static ResourceKey<EquipmentAsset> asset(String path) {
		return ResourceKey.create(EquipmentAssets.ROOT_ID, Wildways.id(path));
	}
}
