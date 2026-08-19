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

	public static final Holder<Potion> UNEASE = Registry.registerForHolder(
		BuiltInRegistries.POTION,
		ResourceKey.create(Registries.POTION, Wildways.id("unease")),
		new Potion("unease", new MobEffectInstance(ModEffects.UNEASE, UNEASE_DURATION))
	);

	private ModPotions() {
	}

	public static void initialize() {
		FabricPotionBrewingBuilder.BUILD.register(builder -> builder.registerPotionRecipe(
			Potions.AWKWARD,
			Ingredient.of(ModItems.ENDERMITE_SHELL),
			UNEASE
		));

		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS)
			.register(output -> {
				output.accept(PotionContents.createItemStack(Items.POTION, UNEASE));
				output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, UNEASE));
				output.accept(PotionContents.createItemStack(Items.LINGERING_POTION, UNEASE));
			});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT)
			.register(output -> output.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, UNEASE)));
	}
}
