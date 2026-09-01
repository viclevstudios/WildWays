package com.viclev.wildways;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Optional;

/** Potion registrations and their chorus-fruit brewing upgrades. */
public final class ModPotions {
	private static final int SECOND = 20;
	private static final int UNEASE_DURATION = SECOND * 60 * 3;
	private static final int STRONG_UNEASE_DURATION = SECOND * 90;

	public static final Holder<Potion> UNEASE = potion("unease", new MobEffectInstance(ModEffects.UNEASE, UNEASE_DURATION));
	public static final Holder<Potion> STRONG_UNEASE = potion("strong_unease", new MobEffectInstance(ModEffects.UNEASE, STRONG_UNEASE_DURATION, 1));
	public static final Holder<Potion> SUPREME_SWIFTNESS = potion("supreme_swiftness", effect(MobEffects.SPEED, 30, 3), effect(MobEffects.HUNGER, 30, 1));
	public static final Holder<Potion> SUPREME_LEAPING = potion("supreme_leaping", effect(MobEffects.JUMP_BOOST, 30, 3), effect(MobEffects.HUNGER, 30, 1));
	public static final Holder<Potion> SUPREME_STRENGTH = potion("supreme_strength", effect(MobEffects.STRENGTH, 30, 2), effect(MobEffects.BLINDNESS, 10, 0));
	public static final Holder<Potion> SUPREME_HEALING = potion("supreme_healing", new MobEffectInstance(MobEffects.INSTANT_HEALTH, 1, 2), effect(MobEffects.SLOWNESS, 30, 1));
	public static final Holder<Potion> SUPREME_REGENERATION = potion("supreme_regeneration", effect(MobEffects.REGENERATION, 20, 2), effect(MobEffects.WEAKNESS, 30, 0));
	public static final Holder<Potion> SUPREME_FIRE_RESISTANCE = potion("supreme_fire_resistance", effect(ModEffects.SUPREME_FIRE_RESISTANCE, 480, 0), effect(MobEffects.HUNGER, 30, 1));
	public static final Holder<Potion> SUPREME_WATER_BREATHING = potion("supreme_water_breathing", effect(MobEffects.WATER_BREATHING, 480, 0), effect(MobEffects.CONDUIT_POWER, 60, 0), effect(MobEffects.NAUSEA, 10, 0));
	public static final Holder<Potion> SUPREME_NIGHT_VISION = potion("supreme_night_vision", effect(ModEffects.SUPREME_NIGHT_VISION, 180, 0), effect(MobEffects.GLOWING, 180, 0), effect(MobEffects.NAUSEA, 10, 0));
	public static final Holder<Potion> SUPREME_INVISIBILITY = potion("supreme_invisibility", hiddenEffect(ModEffects.SUPREME_INVISIBILITY, 480, 0), effect(MobEffects.BLINDNESS, 30, 0));
	public static final Holder<Potion> SUPREME_SLOW_FALLING = potion("supreme_slow_falling", hiddenEffect(ModEffects.SUPREME_SLOW_FALLING, 240, 0), effect(MobEffects.LEVITATION, 10, 0));
	public static final Holder<Potion> FATAL_SLOWNESS = potion("fatal_slowness", effect(MobEffects.SLOWNESS, 30, 4), effect(MobEffects.INVISIBILITY, 10, 0));
	public static final Holder<Potion> FATAL_HARMING = potion("fatal_harming", new MobEffectInstance(MobEffects.INSTANT_DAMAGE, 1, 2), effect(MobEffects.SPEED, 30, 1));
	public static final Holder<Potion> FATAL_POISON = potion("fatal_poison", effect(MobEffects.POISON, 10, 2), effect(MobEffects.STRENGTH, 30, 0));
	public static final Holder<Potion> FATAL_WEAKNESS = potion("fatal_weakness", effect(MobEffects.WEAKNESS, 30, 2), effect(MobEffects.RESISTANCE, 30, 0));

	private ModPotions() {
	}

	public static void initialize() {
		FabricPotionBrewingBuilder.BUILD.register(builder -> {
			builder.registerPotionRecipe(Potions.AWKWARD, Ingredient.of(ModItems.ENDERMITE_SHELL), UNEASE);
			builder.registerPotionRecipe(UNEASE, Ingredient.of(Items.GLOWSTONE_DUST), STRONG_UNEASE);
			registerChorusRecipe(builder, Potions.STRONG_SWIFTNESS, SUPREME_SWIFTNESS);
			registerChorusRecipe(builder, Potions.STRONG_LEAPING, SUPREME_LEAPING);
			registerChorusRecipe(builder, Potions.STRONG_STRENGTH, SUPREME_STRENGTH);
			registerChorusRecipe(builder, Potions.STRONG_HEALING, SUPREME_HEALING);
			registerChorusRecipe(builder, Potions.STRONG_REGENERATION, SUPREME_REGENERATION);
			registerChorusRecipe(builder, Potions.LONG_FIRE_RESISTANCE, SUPREME_FIRE_RESISTANCE);
			registerChorusRecipe(builder, Potions.LONG_WATER_BREATHING, SUPREME_WATER_BREATHING);
			registerChorusRecipe(builder, Potions.LONG_NIGHT_VISION, SUPREME_NIGHT_VISION);
			registerChorusRecipe(builder, Potions.LONG_INVISIBILITY, SUPREME_INVISIBILITY);
			registerChorusRecipe(builder, Potions.LONG_SLOW_FALLING, SUPREME_SLOW_FALLING);
			registerChorusRecipe(builder, Potions.STRONG_SLOWNESS, FATAL_SLOWNESS);
			registerChorusRecipe(builder, Potions.STRONG_HARMING, FATAL_HARMING);
			registerChorusRecipe(builder, Potions.STRONG_POISON, FATAL_POISON);
			registerChorusRecipe(builder, Potions.LONG_WEAKNESS, FATAL_WEAKNESS);
		});

		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(output -> {
			output.accept(PotionContents.createItemStack(Items.POTION, UNEASE));
			output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, UNEASE));
			output.accept(PotionContents.createItemStack(Items.LINGERING_POTION, UNEASE));
			output.accept(PotionContents.createItemStack(Items.POTION, STRONG_UNEASE));
			output.accept(PotionContents.createItemStack(Items.SPLASH_POTION, STRONG_UNEASE));
			output.accept(PotionContents.createItemStack(Items.LINGERING_POTION, STRONG_UNEASE));
			addPotionAfter(output, Potions.STRONG_SWIFTNESS, SUPREME_SWIFTNESS);
			addPotionAfter(output, Potions.STRONG_LEAPING, SUPREME_LEAPING);
			addPotionAfter(output, Potions.STRONG_STRENGTH, SUPREME_STRENGTH);
			addPotionAfter(output, Potions.STRONG_HEALING, SUPREME_HEALING);
			addPotionAfter(output, Potions.STRONG_REGENERATION, SUPREME_REGENERATION);
			addPotionAfter(output, Potions.LONG_FIRE_RESISTANCE, SUPREME_FIRE_RESISTANCE);
			addPotionAfter(output, Potions.LONG_WATER_BREATHING, SUPREME_WATER_BREATHING);
			addPotionAfter(output, Potions.LONG_NIGHT_VISION, SUPREME_NIGHT_VISION);
			addPotionAfter(output, Potions.LONG_INVISIBILITY, SUPREME_INVISIBILITY);
			addPotionAfter(output, Potions.LONG_SLOW_FALLING, SUPREME_SLOW_FALLING);
			addPotionAfter(output, Potions.STRONG_SLOWNESS, FATAL_SLOWNESS);
			addPotionAfter(output, Potions.STRONG_HARMING, FATAL_HARMING);
			addPotionAfter(output, Potions.STRONG_POISON, FATAL_POISON);
			addPotionAfter(output, Potions.LONG_WEAKNESS, FATAL_WEAKNESS);
		});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(output -> {
			output.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, UNEASE));
			output.accept(PotionContents.createItemStack(Items.TIPPED_ARROW, STRONG_UNEASE));
			addTippedArrowAfter(output, Potions.STRONG_SWIFTNESS, SUPREME_SWIFTNESS);
			addTippedArrowAfter(output, Potions.STRONG_LEAPING, SUPREME_LEAPING);
			addTippedArrowAfter(output, Potions.STRONG_STRENGTH, SUPREME_STRENGTH);
			addTippedArrowAfter(output, Potions.STRONG_HEALING, SUPREME_HEALING);
			addTippedArrowAfter(output, Potions.STRONG_REGENERATION, SUPREME_REGENERATION);
			addTippedArrowAfter(output, Potions.LONG_FIRE_RESISTANCE, SUPREME_FIRE_RESISTANCE);
			addTippedArrowAfter(output, Potions.LONG_WATER_BREATHING, SUPREME_WATER_BREATHING);
			addTippedArrowAfter(output, Potions.LONG_NIGHT_VISION, SUPREME_NIGHT_VISION);
			addTippedArrowAfter(output, Potions.LONG_INVISIBILITY, SUPREME_INVISIBILITY);
			addTippedArrowAfter(output, Potions.LONG_SLOW_FALLING, SUPREME_SLOW_FALLING);
			addTippedArrowAfter(output, Potions.STRONG_SLOWNESS, FATAL_SLOWNESS);
			addTippedArrowAfter(output, Potions.STRONG_HARMING, FATAL_HARMING);
			addTippedArrowAfter(output, Potions.STRONG_POISON, FATAL_POISON);
			addTippedArrowAfter(output, Potions.LONG_WEAKNESS, FATAL_WEAKNESS);
		});
	}

	private static void registerChorusRecipe(FabricPotionBrewingBuilder builder, Holder<Potion> input, Holder<Potion> output) {
		builder.registerPotionRecipe(input, Ingredient.of(Items.POPPED_CHORUS_FRUIT), output);
	}

	private static void addPotionAfter(net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput output, Holder<Potion> base, Holder<Potion> upgrade) {
		output.insertAfter(PotionContents.createItemStack(Items.POTION, base), PotionContents.createItemStack(Items.POTION, upgrade));
		output.insertAfter(PotionContents.createItemStack(Items.SPLASH_POTION, base), PotionContents.createItemStack(Items.SPLASH_POTION, upgrade));
		output.insertAfter(PotionContents.createItemStack(Items.LINGERING_POTION, base), PotionContents.createItemStack(Items.LINGERING_POTION, upgrade));
	}

	private static void addTippedArrowAfter(net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput output, Holder<Potion> base, Holder<Potion> upgrade) {
		output.insertAfter(PotionContents.createItemStack(Items.TIPPED_ARROW, base), PotionContents.createItemStack(Items.TIPPED_ARROW, upgrade));
	}

	private static MobEffectInstance effect(Holder<MobEffect> effect, int seconds, int amplifier) {
		return new MobEffectInstance(effect, seconds * SECOND, amplifier);
	}

	private static MobEffectInstance hiddenEffect(Holder<MobEffect> effect, int seconds, int amplifier) {
		return new MobEffectInstance(effect, seconds * SECOND, amplifier, false, false, true);
	}

	/** Returns the vanilla potion whose colour and item tint this upgrade reuses. */
	public static Optional<Holder<Potion>> visualBase(Holder<Potion> potion) {
		if (potion.equals(SUPREME_SWIFTNESS)) return Optional.of(Potions.STRONG_SWIFTNESS);
		if (potion.equals(SUPREME_LEAPING)) return Optional.of(Potions.STRONG_LEAPING);
		if (potion.equals(SUPREME_STRENGTH)) return Optional.of(Potions.STRONG_STRENGTH);
		if (potion.equals(SUPREME_HEALING)) return Optional.of(Potions.STRONG_HEALING);
		if (potion.equals(SUPREME_REGENERATION)) return Optional.of(Potions.STRONG_REGENERATION);
		if (potion.equals(SUPREME_FIRE_RESISTANCE)) return Optional.of(Potions.LONG_FIRE_RESISTANCE);
		if (potion.equals(SUPREME_WATER_BREATHING)) return Optional.of(Potions.LONG_WATER_BREATHING);
		if (potion.equals(SUPREME_NIGHT_VISION)) return Optional.of(Potions.LONG_NIGHT_VISION);
		if (potion.equals(SUPREME_INVISIBILITY)) return Optional.of(Potions.LONG_INVISIBILITY);
		if (potion.equals(SUPREME_SLOW_FALLING)) return Optional.of(Potions.LONG_SLOW_FALLING);
		if (potion.equals(FATAL_SLOWNESS)) return Optional.of(Potions.STRONG_SLOWNESS);
		if (potion.equals(FATAL_HARMING)) return Optional.of(Potions.STRONG_HARMING);
		if (potion.equals(FATAL_POISON)) return Optional.of(Potions.STRONG_POISON);
		if (potion.equals(FATAL_WEAKNESS)) return Optional.of(Potions.LONG_WEAKNESS);
		return Optional.empty();
	}

	private static Holder<Potion> potion(String name, MobEffectInstance... effects) {
		return Registry.registerForHolder(BuiltInRegistries.POTION, ResourceKey.create(Registries.POTION, Wildways.id(name)), new Potion(name, effects));
	}
}
