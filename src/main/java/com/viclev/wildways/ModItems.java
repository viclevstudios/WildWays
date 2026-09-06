package com.viclev.wildways;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ArrowItem;

import java.util.function.Function;
import java.util.List;
import java.util.Set;

public final class ModItems {
	public static final Item ENDERMITE_SHELL = register(
		ModItemIds.ENDERMITE_SHELL,
		Item::new,
		new Item.Properties()
	);
	public static final Item BIOME_COMPASS = register(
		ModItemIds.BIOME_COMPASS,
		Item::new,
		new Item.Properties()
	);
	public static final ArrowItem TURTLE_ARROW = register(ModItemIds.TURTLE_ARROW, TurtleArrowItem::new, new Item.Properties());
	public static final ArrowItem RANGE_ARROW = register(ModItemIds.RANGE_ARROW, RangeArrowItem::new, new Item.Properties());
	public static final ArrowItem EXPLOSIVE_ARROW = register(ModItemIds.EXPLOSIVE_ARROW, ExplosiveArrowItem::new, new Item.Properties());
	public static final PortalEyeItem EYE_OF_ICE = portalEye(ModItemIds.EYE_OF_ICE, false);
	public static final PortalEyeItem EYE_OF_THE_BRUTE = portalEye(ModItemIds.EYE_OF_THE_BRUTE, false);
	public static final PortalEyeItem EYE_OF_STORM = portalEye(ModItemIds.EYE_OF_STORM, false);
	public static final PortalEyeItem EYE_OF_WATER = portalEye(ModItemIds.EYE_OF_WATER, false);
	public static final PortalEyeItem LOST_EYE = portalEye(ModItemIds.LOST_EYE, false);
	public static final PortalEyeItem EYE_OF_ILLAGERS = portalEye(ModItemIds.EYE_OF_ILLAGERS, false);
	public static final PortalEyeItem EYE_OF_THE_TIGER = portalEye(ModItemIds.EYE_OF_THE_TIGER, false);
	public static final PortalEyeItem EYE_OF_DARKNESS = portalEye(ModItemIds.EYE_OF_DARKNESS, false);
	public static final PortalEyeItem EYE_OF_THE_CREAKING = portalEye(ModItemIds.EYE_OF_THE_CREAKING, false);
	public static final PortalEyeItem EYE_OF_BREWING = portalEye(ModItemIds.EYE_OF_BREWING, false);
	public static final PortalEyeItem ENCHANTED_EYE = portalEye(ModItemIds.ENCHANTED_EYE, true);
	public static final PortalEyeItem EYE_OF_ENDERMITES = portalEye(ModItemIds.EYE_OF_ENDERMITES, false);
	public static final Item AWKWARD_EYE = register(ModItemIds.AWKWARD_EYE, Item::new, new Item.Properties().stacksTo(16));
	public static final Item THICK_EYE = register(ModItemIds.THICK_EYE, Item::new, new Item.Properties().stacksTo(16));

	public static final List<PortalEyeItem> PORTAL_EYES = List.of(
		EYE_OF_ICE,
		EYE_OF_THE_BRUTE,
		EYE_OF_STORM,
		EYE_OF_WATER,
		LOST_EYE,
		EYE_OF_ILLAGERS,
		EYE_OF_THE_TIGER,
		EYE_OF_DARKNESS,
		EYE_OF_THE_CREAKING,
		EYE_OF_BREWING,
		ENCHANTED_EYE,
		EYE_OF_ENDERMITES
	);
	public static final Set<Item> PORTAL_EYE_SET = Set.copyOf(PORTAL_EYES);

	private ModItems() {
	}

	private static <T extends Item> T register(
		ResourceKey<Item> itemKey,
		Function<Item.Properties, T> itemFactory,
		Item.Properties properties
	) {
		T item = itemFactory.apply(properties.setId(itemKey));
		return Registry.register(BuiltInRegistries.ITEM, itemKey, item);
	}

	private static PortalEyeItem portalEye(ResourceKey<Item> key, boolean requiresEnchantment) {
		return register(
			key,
			properties -> new PortalEyeItem(properties, requiresEnchantment),
			requiresEnchantment
				? new Item.Properties().stacksTo(16).enchantable(10)
				: new Item.Properties().stacksTo(16)
		);
	}

	public static boolean isPortalEye(Item item) {
		return PORTAL_EYE_SET.contains(item);
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
			.register(output -> {
				output.accept(ENDERMITE_SHELL);
				output.accept(AWKWARD_EYE);
				output.accept(THICK_EYE);
				PORTAL_EYES.forEach(output::accept);
			});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
			.register(output -> output.accept(BIOME_COMPASS));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT)
			.register(output -> {
				output.accept(TURTLE_ARROW);
				output.accept(RANGE_ARROW);
				output.accept(EXPLOSIVE_ARROW);
			});
	}
}
