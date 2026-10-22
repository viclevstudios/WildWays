package com.viclev.wildways;

import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.enchantment.Enchantments;

/** Covers actual vanilla trim recipes, component preservation and the Volcanite palette override. */
public class VolcaniteTrimGameTests {
	@GameTest
	public void ingotsWorkWithEveryVanillaTrimPattern(GameTestHelper helper) {
		ItemStack ingot = new ItemStack(ModItems.VOLCANITE);
		helper.assertTrue(ingot.is(ItemTags.TRIM_MATERIALS), "Volcanite must be accepted by vanilla trim recipes");
		helper.assertTrue(ingot.get(DataComponents.PROVIDES_TRIM_MATERIAL).is(ModTrimMaterials.VOLCANITE),
			"The normal Volcanite ingot must provide the registered trim material");
		for (String pattern : List.of("spire", "shaper", "host", "tide", "sentry", "rib", "eye", "ward", "snout",
			"wayfinder", "raiser", "dune", "vex", "flow", "wild", "silence", "bolt", "coast")) {
			ItemStack output = trim(helper, new ItemStack(Items.IRON_CHESTPLATE), pattern, ModItems.VOLCANITE);
			var trim = output.get(DataComponents.TRIM);
			helper.assertTrue(trim.material().is(ModTrimMaterials.VOLCANITE)
				&& trim.pattern().is(ResourceKey.create(Registries.TRIM_PATTERN, Identifier.withDefaultNamespace(pattern))),
				"The selected pattern must work with Volcanite: " + pattern);
		}
		helper.succeed();
	}

	@GameTest
	public void allArmorKeepsNamesDamageEnchantmentsAndDyes(GameTestHelper helper) {
		var unbreaking = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING);
		int checked = 0;
		for (Item item : BuiltInRegistries.ITEM) {
			ItemStack base = new ItemStack(item);
			if (!base.is(ItemTags.TRIMMABLE_ARMOR)) continue;
			base.set(DataComponents.CUSTOM_NAME, Component.literal("Wildways trim test"));
			base.setDamageValue(7);
			base.enchant(unbreaking, 2);
			if (BuiltInRegistries.ITEM.getKey(item).getPath().startsWith("leather_")) {
				base.set(DataComponents.DYED_COLOR, new DyedItemColor(0x336699));
			}
			ItemStack output = trim(helper, base, "sentry", ModItems.VOLCANITE);
			helper.assertTrue(output.get(DataComponents.TRIM).material().is(ModTrimMaterials.VOLCANITE),
				"Every trimmable armor item must accept Volcanite");
			output.remove(DataComponents.TRIM);
			helper.assertTrue(ItemStack.isSameItemSameComponents(base, output),
				"Trimming must preserve all original armor data, including dye colors");
			checked++;
		}
		helper.assertTrue(checked >= 41, "Check all 29 vanilla and 12 Wildways armor items");
		helper.succeed();
	}

	@GameTest
	public void volcaniteArmorSelectsContrastAndOtherTrimsStillWork(GameTestHelper helper) {
		ItemStack redstone = trim(helper, new ItemStack(ModItems.VOLCANITE_CHESTPLATE), "sentry", Items.REDSTONE);
		helper.assertTrue(redstone.get(DataComponents.TRIM).material().is(
			ResourceKey.create(Registries.TRIM_MATERIAL, Identifier.withDefaultNamespace("redstone"))), "Existing redstone trims must keep working");
		ItemStack output = trim(helper, redstone, "sentry", ModItems.VOLCANITE);
		var trim = output.get(DataComponents.TRIM);
		helper.assertTrue(trim.layerAssetId("trims/entity/humanoid", ProgressionMaterials.VOLCANITE_ASSET).equals(
			Identifier.withDefaultNamespace("trims/entity/humanoid/sentry_wildways_volcanite_contrast")),
			"Volcanite armor must use the contrast palette");
		helper.assertTrue(trim.layerAssetId("trims/entity/humanoid", EquipmentAssets.IRON).equals(
			Identifier.withDefaultNamespace("trims/entity/humanoid/sentry_wildways_volcanite")),
			"Other armor must use the normal Volcanite palette");
		var repeated = new SmithingRecipeInput(new ItemStack(Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE), output, new ItemStack(ModItems.VOLCANITE));
		var recipe = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.SMITHING, repeated, helper.getLevel());
		helper.assertTrue(recipe.isPresent() && recipe.orElseThrow().value().assemble(repeated).isEmpty(),
			"Reapplying the identical trim must not produce another result");
		helper.succeed();
	}

	private ItemStack trim(GameTestHelper helper, ItemStack base, String pattern, Item material) {
		Item template = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(pattern + "_armor_trim_smithing_template"));
		var input = new SmithingRecipeInput(new ItemStack(template), base.copy(), new ItemStack(material));
		var recipe = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.SMITHING, input, helper.getLevel());
		helper.assertTrue(recipe.isPresent(), "Vanilla trim recipe must match the selected armor and material");
		ItemStack output = recipe.orElseThrow().value().assemble(input);
		helper.assertFalse(output.isEmpty(), "Applying a different trim must return the decorated armor");
		return output;
	}
}
