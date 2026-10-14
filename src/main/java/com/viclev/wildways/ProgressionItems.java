package com.viclev.wildways;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.List;
import java.util.function.Function;

public final class ProgressionItems {
	public static final Item QUARTZ_SWORD = sword("quartz_sword", ProgressionMaterials.QUARTZ_TOOL, ProgressionMaterials.SWORD_ATTACK_DAMAGE, ProgressionMaterials.SWORD_ATTACK_SPEED);
	public static final Item QUARTZ_PICKAXE = pickaxe("quartz_pickaxe", ProgressionMaterials.QUARTZ_TOOL);
	public static final Item QUARTZ_AXE = axe("quartz_axe", ProgressionMaterials.QUARTZ_TOOL, ProgressionMaterials.AXE_ATTACK_DAMAGE, ProgressionMaterials.AXE_ATTACK_SPEED);
	public static final Item QUARTZ_SHOVEL = shovel("quartz_shovel", ProgressionMaterials.QUARTZ_TOOL, ProgressionMaterials.SHOVEL_ATTACK_DAMAGE, ProgressionMaterials.SHOVEL_ATTACK_SPEED);
	public static final Item QUARTZ_HOE = hoe("quartz_hoe", ProgressionMaterials.QUARTZ_TOOL, ProgressionMaterials.HOE_ATTACK_DAMAGE, ProgressionMaterials.HOE_ATTACK_SPEED);
	public static final Item QUARTZ_HELMET = armor("quartz_helmet", ProgressionMaterials.QUARTZ_ARMOR, ArmorType.HELMET, false, false);
	public static final Item QUARTZ_CHESTPLATE = armor("quartz_chestplate", ProgressionMaterials.QUARTZ_ARMOR, ArmorType.CHESTPLATE, false, false);
	public static final Item QUARTZ_LEGGINGS = armor("quartz_leggings", ProgressionMaterials.QUARTZ_ARMOR, ArmorType.LEGGINGS, false, false);
	public static final Item QUARTZ_BOOTS = armor("quartz_boots", ProgressionMaterials.QUARTZ_ARMOR, ArmorType.BOOTS, false, false);

	public static final Item AMETHYST_SWORD = sword("amethyst_sword", ProgressionMaterials.AMETHYST_TOOL, ProgressionMaterials.SWORD_ATTACK_DAMAGE, ProgressionMaterials.SWORD_ATTACK_SPEED + ProgressionMaterials.AMETHYST_ATTACK_SPEED_BONUS);
	public static final Item AMETHYST_PICKAXE = pickaxe("amethyst_pickaxe", ProgressionMaterials.AMETHYST_TOOL, ProgressionMaterials.PICKAXE_ATTACK_SPEED + ProgressionMaterials.AMETHYST_ATTACK_SPEED_BONUS);
	public static final Item AMETHYST_AXE = axe("amethyst_axe", ProgressionMaterials.AMETHYST_TOOL, ProgressionMaterials.AXE_ATTACK_DAMAGE, ProgressionMaterials.AXE_ATTACK_SPEED + ProgressionMaterials.AMETHYST_ATTACK_SPEED_BONUS);
	public static final Item AMETHYST_SHOVEL = shovel("amethyst_shovel", ProgressionMaterials.AMETHYST_TOOL, ProgressionMaterials.SHOVEL_ATTACK_DAMAGE, ProgressionMaterials.SHOVEL_ATTACK_SPEED + ProgressionMaterials.AMETHYST_ATTACK_SPEED_BONUS);
	public static final Item AMETHYST_HOE = hoe("amethyst_hoe", ProgressionMaterials.AMETHYST_TOOL, ProgressionMaterials.HOE_ATTACK_DAMAGE, ProgressionMaterials.HOE_ATTACK_SPEED + ProgressionMaterials.AMETHYST_ATTACK_SPEED_BONUS);
	public static final Item AMETHYST_HELMET = armor("amethyst_helmet", ProgressionMaterials.AMETHYST_ARMOR, ArmorType.HELMET, true, false);
	public static final Item AMETHYST_CHESTPLATE = armor("amethyst_chestplate", ProgressionMaterials.AMETHYST_ARMOR, ArmorType.CHESTPLATE, true, false);
	public static final Item AMETHYST_LEGGINGS = armor("amethyst_leggings", ProgressionMaterials.AMETHYST_ARMOR, ArmorType.LEGGINGS, true, false);
	public static final Item AMETHYST_BOOTS = armor("amethyst_boots", ProgressionMaterials.AMETHYST_ARMOR, ArmorType.BOOTS, true, false);

	public static final Item GOLD_SWORD = sword("gold_sword", ProgressionMaterials.GOLD_TOOL, ProgressionMaterials.SWORD_ATTACK_DAMAGE, ProgressionMaterials.SWORD_ATTACK_SPEED);
	public static final Item GOLD_PICKAXE = pickaxe("gold_pickaxe", ProgressionMaterials.GOLD_TOOL);
	public static final Item GOLD_AXE = axe("gold_axe", ProgressionMaterials.GOLD_TOOL, ProgressionMaterials.AXE_ATTACK_DAMAGE, ProgressionMaterials.AXE_ATTACK_SPEED);
	public static final Item GOLD_SHOVEL = shovel("gold_shovel", ProgressionMaterials.GOLD_TOOL, ProgressionMaterials.SHOVEL_ATTACK_DAMAGE, ProgressionMaterials.SHOVEL_ATTACK_SPEED);
	public static final Item GOLD_HOE = hoe("gold_hoe", ProgressionMaterials.GOLD_TOOL, ProgressionMaterials.HOE_ATTACK_DAMAGE, ProgressionMaterials.HOE_ATTACK_SPEED);
	public static final Item GOLD_HELMET = Items.GOLDEN_HELMET;
	public static final Item GOLD_CHESTPLATE = Items.GOLDEN_CHESTPLATE;
	public static final Item GOLD_LEGGINGS = Items.GOLDEN_LEGGINGS;
	public static final Item GOLD_BOOTS = Items.GOLDEN_BOOTS;

	public static final Item REFINEMENT_QUARTZ = refinement("quartz_refinement", "quartz", List.of("helmet", "chestplate", "leggings", "boots", "sword", "pickaxe", "axe", "shovel", "hoe"), "quartz");
	public static final Item REFINEMENT_AMETHYST = refinement("amethyst_refinement", "amethyst", List.of("helmet", "chestplate", "leggings", "boots", "sword", "pickaxe", "axe", "shovel", "hoe"), "amethyst_shard");
	public static final Item REFINEMENT_GOLD = refinement("gold_refinement", "gold", List.of("helmet", "chestplate", "leggings", "boots", "sword", "pickaxe", "axe", "shovel", "hoe"), "ingot");
	public static final Item REFINEMENT_DIAMOND = refinement("diamond_refinement", "diamond", List.of("helmet", "chestplate", "leggings", "boots", "sword", "pickaxe", "axe", "shovel", "hoe"), "diamond");
	public static final Item REFINEMENT_VOLCANITE = refinement("volcanite_refinement", "volcanite", List.of("helmet", "chestplate", "leggings", "boots", "sword", "pickaxe", "axe", "shovel", "hoe"), "ingot");

	public static final List<Item> QUARTZ_SET = List.of(QUARTZ_HELMET, QUARTZ_CHESTPLATE, QUARTZ_LEGGINGS, QUARTZ_BOOTS);
	public static final List<Item> AMETHYST_SET = List.of(AMETHYST_HELMET, AMETHYST_CHESTPLATE, AMETHYST_LEGGINGS, AMETHYST_BOOTS);
	public static final List<Item> GOLD_SET = List.of(GOLD_HELMET, GOLD_CHESTPLATE, GOLD_LEGGINGS, GOLD_BOOTS);
	public static final List<Item> QUARTZ_TOOLS = List.of(QUARTZ_SWORD, QUARTZ_PICKAXE, QUARTZ_AXE, QUARTZ_SHOVEL, QUARTZ_HOE);
	public static final List<Item> AMETHYST_TOOLS = List.of(AMETHYST_SWORD, AMETHYST_PICKAXE, AMETHYST_AXE, AMETHYST_SHOVEL, AMETHYST_HOE);
	public static final List<Item> GOLD_TOOLS = List.of(GOLD_SWORD, GOLD_PICKAXE, GOLD_AXE, GOLD_SHOVEL, GOLD_HOE);
	public static final List<Item> REFINEMENTS = List.of(REFINEMENT_QUARTZ, REFINEMENT_AMETHYST, REFINEMENT_GOLD, REFINEMENT_DIAMOND, REFINEMENT_VOLCANITE);

	private ProgressionItems() {
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> {
			output.insertAfter(Items.IRON_PICKAXE, QUARTZ_PICKAXE);
			output.insertAfter(QUARTZ_PICKAXE, AMETHYST_PICKAXE);
			output.insertAfter(Items.GOLDEN_PICKAXE, GOLD_PICKAXE);
			output.insertAfter(Items.IRON_AXE, QUARTZ_AXE);
			output.insertAfter(QUARTZ_AXE, AMETHYST_AXE);
			output.insertAfter(Items.IRON_SHOVEL, QUARTZ_SHOVEL);
			output.insertAfter(QUARTZ_SHOVEL, AMETHYST_SHOVEL);
			output.insertAfter(Items.IRON_HOE, QUARTZ_HOE);
			output.insertAfter(QUARTZ_HOE, AMETHYST_HOE);
			output.insertAfter(Items.IRON_SWORD, QUARTZ_SWORD);
			output.insertAfter(QUARTZ_SWORD, AMETHYST_SWORD);
			output.insertAfter(Items.GOLDEN_SWORD, GOLD_SWORD);
			output.insertAfter(Items.GOLDEN_AXE, GOLD_AXE);
			output.insertAfter(Items.GOLDEN_SHOVEL, GOLD_SHOVEL);
			output.insertAfter(Items.GOLDEN_HOE, GOLD_HOE);
			output.insertAfter(Items.IRON_PICKAXE, REFINEMENT_QUARTZ);
			output.insertAfter(REFINEMENT_QUARTZ, REFINEMENT_AMETHYST);
			output.insertAfter(REFINEMENT_AMETHYST, REFINEMENT_GOLD);
			output.insertAfter(REFINEMENT_GOLD, REFINEMENT_DIAMOND);
			output.insertAfter(REFINEMENT_DIAMOND, REFINEMENT_VOLCANITE);
		});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(output -> {
			output.insertAfter(Items.IRON_HELMET, QUARTZ_HELMET);
			output.insertAfter(Items.IRON_CHESTPLATE, QUARTZ_CHESTPLATE);
			output.insertAfter(Items.IRON_LEGGINGS, QUARTZ_LEGGINGS);
			output.insertAfter(Items.IRON_BOOTS, QUARTZ_BOOTS);
			output.insertAfter(Items.IRON_HELMET, AMETHYST_HELMET);
			output.insertAfter(Items.IRON_CHESTPLATE, AMETHYST_CHESTPLATE);
			output.insertAfter(Items.IRON_LEGGINGS, AMETHYST_LEGGINGS);
			output.insertAfter(Items.IRON_BOOTS, AMETHYST_BOOTS);
		});
	}

	public static boolean isAmethystArmor(Item item) {
		return AMETHYST_SET.contains(item);
	}

	public static boolean hasAmethystHeritage(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		return data != null && data.copyTag().getBoolean("wildways_amethyst_heritage").orElse(false);
	}

	public static boolean isAmethystArmor(ItemStack stack) {
		return isAmethystArmor(stack.getItem()) || hasAmethystHeritage(stack);
	}

	public static boolean isAmethystTool(Item item) {
		return AMETHYST_TOOLS.contains(item);
	}

	public static boolean isAmethystGear(Item item) {
		return isAmethystArmor(item) || isAmethystTool(item);
	}

	public static boolean isArmorEquipment(Item item) {
		String path = BuiltInRegistries.ITEM.getKey(item).getPath();
		return path.endsWith("_helmet") || path.endsWith("_chestplate") || path.endsWith("_leggings") || path.endsWith("_boots");
	}

	public static boolean isToolEquipment(Item item) {
		String path = BuiltInRegistries.ITEM.getKey(item).getPath();
		return path.endsWith("_sword") || path.endsWith("_pickaxe") || path.endsWith("_axe") || path.endsWith("_shovel") || path.endsWith("_hoe");
	}

	public static Item.Properties volcaniteArmorProperties(ArmorType type) {
		return armorProperties(ProgressionMaterials.VOLCANITE_ARMOR, type, false, true)
			.delayedComponent(DataComponents.DAMAGE_RESISTANT, lookup ->
				new DamageResistant(lookup.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypeTags.IS_EXPLOSION)));
	}

	public static Item.Properties volcaniteProperties() {
		return new Item.Properties().delayedComponent(DataComponents.DAMAGE_RESISTANT, lookup ->
			new DamageResistant(lookup.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypeTags.IS_EXPLOSION)));
	}

	private static Item sword(String name, ToolMaterial material, float damage, float speed) {
		return register(name, Item::new, new Item.Properties().sword(material, damage, speed));
	}

	private static Item pickaxe(String name, ToolMaterial material) {
		return pickaxe(name, material, ProgressionMaterials.PICKAXE_ATTACK_SPEED);
	}

	private static Item pickaxe(String name, ToolMaterial material, float speed) {
		return register(name, Item::new, new Item.Properties().pickaxe(material, ProgressionMaterials.PICKAXE_ATTACK_DAMAGE, speed));
	}

	private static Item axe(String name, ToolMaterial material, float damage, float speed) {
		return register(name, properties -> new AxeItem(material, damage, speed, properties), new Item.Properties());
	}

	private static Item shovel(String name, ToolMaterial material, float damage, float speed) {
		return register(name, properties -> new ShovelItem(material, damage, speed, properties), new Item.Properties());
	}

	private static Item hoe(String name, ToolMaterial material, float damage, float speed) {
		return register(name, properties -> new HoeItem(material, damage, speed, properties), new Item.Properties());
	}

	private static Item armor(String name, ArmorMaterial material, ArmorType type, boolean amethyst, boolean volcanite) {
		return register(name, Item::new, armorProperties(material, type, amethyst, volcanite));
	}

	private static Item.Properties armorProperties(ArmorMaterial material, ArmorType type, boolean amethyst, boolean volcanite) {
		Item.Properties properties = new Item.Properties().humanoidArmor(material, type);
		EquipmentSlotGroup slot = EquipmentSlotGroup.bySlot(type.getSlot());
		ItemAttributeModifiers attributes = material.createAttributes(type);
		if (amethyst) {
			attributes = attributes.withModifierAdded(Attributes.MOVEMENT_SPEED,
				new AttributeModifier(Wildways.id("amethyst_armor_speed_" + type.getName()), ProgressionMaterials.AMETHYST_SPEED_PER_PIECE, AttributeModifier.Operation.ADD_VALUE), slot);
		}
		if (volcanite) {
			attributes = attributes.withModifierAdded(Attributes.BLOCK_INTERACTION_RANGE,
				new AttributeModifier(Wildways.id("volcanite_block_range_" + type.getName()), ProgressionMaterials.VOLCANITE_RANGE_PER_PIECE, AttributeModifier.Operation.ADD_VALUE), slot);
		}
		return properties.attributes(attributes);
	}

	private static Item refinement(String id, String name, List<String> baseSlots, String additionSlot) {
		List<Identifier> baseIcons = baseSlots.stream().map(slot -> Identifier.withDefaultNamespace("container/slot/" + slot)).toList();
		return register(id, properties -> new SmithingTemplateItem(
			 net.minecraft.network.chat.Component.translatable("item.wildways." + name + "_refinement.applies_to"),
			 net.minecraft.network.chat.Component.translatable("item.wildways." + name + "_refinement.ingredients"),
			 net.minecraft.network.chat.Component.translatable("item.wildways.refinement.base_slot"),
			 net.minecraft.network.chat.Component.translatable("item.wildways." + name + "_refinement.additions_slot"),
			 baseIcons,
			 List.of(Identifier.withDefaultNamespace("container/slot/" + additionSlot)),
			 properties
		), new Item.Properties().stacksTo(64));
	}

	private static Item register(String id, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, Wildways.id(id));
		Item item = factory.apply(properties.setId(key));
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}
}
