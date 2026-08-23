package com.viclev.wildways;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;

public final class ModPotions {
	private static final int UNEASE_DURATION = 20 * 60 * 3;
	private static final int STRONG_UNEASE_DURATION = 20 * 60 + 20 * 30;

	public static final Holder<Potion> UNEASE = Registry.registerForHolder(
		BuiltInRegistries.POTION,
		ResourceKey.create(Registries.POTION, Wildways.id("unease")),
		new Potion("unease", new MobEffectInstance(ModEffects.UNEASE, UNEASE_DURATION))
	);
	public static final Holder<Potion> STRONG_UNEASE = Registry.registerForHolder(
		BuiltInRegistries.POTION,
		ResourceKey.create(Registries.POTION, Wildways.id("strong_unease")),
		new Potion("strong_unease", new MobEffectInstance(ModEffects.UNEASE, STRONG_UNEASE_DURATION, 1))
	);

	private ModPotions() {
	}

	public static void initialize() {
		FabricPotionBrewingBuilder.BUILD.register(builder -> builder.registerPotionRecipe(
			Potions.AWKWARD,
			Ingredient.of(ModItems.ENDERMITE_SHELL),
			UNEASE
		));
		FabricPotionBrewingBuilder.BUILD.register(builder -> builder.registerPotionRecipe(
			UNEASE,
			Ingredient.of(Items.GLOWSTONE_DUST),
			STRONG_UNEASE
		));

		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS)
			.register(output -> {
				output.accept(PotionContents.createItemStack(Items.POTION, UNEASE));
				output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, UNEASE));
				output.accept(PotionContents.createItemStack(Items.LINGERING_POTION, UNEASE));
				output.accept(PotionContents.createItemStack(Items.POTION, STRONG_UNEASE));
				output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, STRONG_UNEASE));
				output.accept(PotionContents.createItemStack(Items.LINGERING_POTION, STRONG_UNEASE));
			});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT)
			.register(output -> {
				output.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, UNEASE));
				output.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, STRONG_UNEASE));
			});
	}
}
