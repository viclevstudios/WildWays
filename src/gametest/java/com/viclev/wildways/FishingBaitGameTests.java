package com.viclev.wildways;

import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantments;

public class FishingBaitGameTests {
	@GameTest
	public void baitRecipesPreserveTheOriginalRod(GameTestHelper helper) {
		checkRecipe(helper, Items.WHEAT, Items.BEETROOT, ModItems.BEETROOT_DOUGH, FishingBait.Type.BEETROOT_DOUGH);
		checkRecipe(helper, Items.SPIDER_EYE, Items.SUGAR, ModItems.CANDIED_BAIT, FishingBait.Type.CANDIED_BAIT);
		checkRecipe(helper, Items.COPPER_INGOT, Items.AMETHYST_SHARD, ModItems.AMETHYST_BLINKER, FishingBait.Type.AMETHYST_BLINKER);
		helper.succeed();
	}

	@GameTest
	public void richFishGivesMoreHungerAndSaturation(GameTestHelper helper) {
		for (Item item : List.of(Items.COD, Items.SALMON, Items.TROPICAL_FISH)) {
			int rawNutrition = item == Items.TROPICAL_FISH ? 2 : 3;
			ItemStack raw = item.getDefaultInstance();
			FishingBait.enrichFish(raw);
			assertRichFish(helper, raw, rawNutrition);
			FishingBait.enrichFish(raw);
			assertRichFish(helper, raw, rawNutrition);
			if (item == Items.COD || item == Items.SALMON) {
				var input = new SingleRecipeInput(raw);
				var level = helper.getLevel();
				for (var recipeType : List.of(RecipeType.SMELTING, RecipeType.SMOKING, RecipeType.CAMPFIRE_COOKING)) {
					var recipe = level.recipeAccess().getRecipeFor(recipeType, input, level).orElseThrow().value();
					assertRichFish(helper, recipe.assemble(input), item == Items.COD ? 7 : 8);
					helper.assertFalse(recipe.assemble(new SingleRecipeInput(item.getDefaultInstance())).has(FishingBait.RICH_FISH),
						"Ordinary fish must remain ordinary after cooking");
				}
			}
		}
		ItemStack pufferfish = new ItemStack(Items.PUFFERFISH);
		FishingBait.enrichFish(pufferfish);
		helper.assertTrue(ItemStack.matches(pufferfish, new ItemStack(Items.PUFFERFISH)), "New pufferfish should stay ordinary");
		helper.succeed();
	}

	@GameTest
	public void savedRichFishUsesTheCurrentNutrition(GameTestHelper helper) {
		var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
		for (var entry : Map.of(Items.COD, 3, Items.SALMON, 3, Items.TROPICAL_FISH, 2,
			Items.COOKED_COD, 7, Items.COOKED_SALMON, 8, Items.PUFFERFISH, 0).entrySet()) {
			ItemStack old = new ItemStack(entry.getKey(), 4);
			var food = old.get(DataComponents.FOOD);
			old.set(DataComponents.FOOD, new FoodProperties(food.nutrition(), food.saturation() * 1.3F, food.canAlwaysEat()));
			old.set(FishingBait.RICH_FISH, Unit.INSTANCE);
			ItemStack restored = ItemStack.CODEC.parse(ops, ItemStack.CODEC.encodeStart(ops, old).getOrThrow()).getOrThrow();
			if (entry.getKey() == Items.PUFFERFISH) {
				helper.assertTrue(ItemStack.matches(restored, new ItemStack(Items.PUFFERFISH, 4)), "Old rich pufferfish should return to normal");
			} else {
				assertRichFish(helper, restored, entry.getValue());
				helper.assertTrue(restored.getCount() == 4, "Updating the fish must preserve its stack size");
			}
		}
		helper.succeed();
	}

	@GameTest
	public void baitOddsAndCandiedRoundRules(GameTestHelper helper) {
		var dough = FishingBait.LootChances.forBait(FishingBait.Type.BEETROOT_DOUGH, true, 0);
		var candied = FishingBait.LootChances.forBait(FishingBait.Type.CANDIED_BAIT, true, 3);
		var blinker = FishingBait.LootChances.forBait(FishingBait.Type.AMETHYST_BLINKER, true, 3);
		var confined = FishingBait.LootChances.forBait(FishingBait.Type.AMETHYST_BLINKER, false, 3);
		helper.assertTrue(close(dough.junk(), 0.05F) && close(dough.treasure(), 0.05F), "Beetroot Dough should shift 5 percentage points of junk to fish");
		helper.assertTrue(close(candied.junk(), 2 / 97.0F) && close(candied.treasure(), 11 / 97.0F), "Candied Bait should halve junk with Luck of the Sea III");
		helper.assertTrue(close(blinker.treasure(), 22 / 97.0F) && close(confined.treasure(), 0), "Blinker should double treasure only in open water");
		var standard = FishingMinigame.Rules.forRound(false);
		var wider = FishingMinigame.Rules.forRound(true);
		helper.assertTrue(standard.duration() == 5_000 && wider.duration() == 3_000
			&& close(standard.centerHalfWidth(), 0.0675F) && close(standard.edgeHalfWidth(), 0.12F)
			&& close(wider.centerHalfWidth(), standard.centerHalfWidth() * 1.5F)
			&& close(wider.edgeHalfWidth(), standard.edgeHalfWidth() * 1.5F), "Candied Bait should use a shorter, wider round");
		helper.assertTrue(wider.points(FishingMinigame.pointerSample(0.5F), 0.5F) == 2
			&& wider.points(FishingMinigame.pointerSample(0.66F), 0.5F) == 1
			&& wider.points(FishingMinigame.pointerSample(0.8F), 0.5F) == 0, "The three target zones must award full, half, or zero points");
		for (var rules : List.of(standard, wider)) {
			int samples = rules.sampleCount();
			helper.assertTrue(close(rules.catchChance(2 * samples), 1) && close(rules.catchChance(samples), 0.5F)
				&& close(rules.catchChance(0), 0), "A full round in one zone must yield 100%, 50%, or 0%");
			helper.assertTrue(close(rules.catchChance(2 * (samples / 2) + samples / 2), 0.75F), "Half green and half yellow should yield 75%");
		}
		var path = FishingMinigame.Path.fromSeed(13);
		helper.assertTrue(close(path.targetAt(FishingMinigame.READY_MILLIS + 876), path.firstTurn()),
			"The first turn should arrive 1.75 times as fast as before");
		helper.assertTrue(!close(path.targetAt(4_000), path.targetAt(4_300)), "The target must keep moving after finishing its first pass");
		helper.assertTrue(!close(path.targetAt(wider.duration()), path.targetAt(standard.duration())),
			"The shortened round should finish partway through the shared path");
		helper.succeed();
	}

	private static void checkRecipe(GameTestHelper helper, Item first, Item second, Item baitItem, FishingBait.Type type) {
		ItemStack bait = craft(helper, new ItemStack(first), new ItemStack(second));
		helper.assertTrue(bait.is(baitItem) && bait.getCount() == 1, "The two ingredients must craft one bait");
		ItemStack rod = new ItemStack(Items.FISHING_ROD);
		rod.setDamageValue(9);
		rod.set(DataComponents.CUSTOM_NAME, Component.literal("River rod"));
		rod.enchant(helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LURE), 2);
		ItemStack loaded = craft(helper, bait, rod);
		var full = new FishingBait(type, 16);
		helper.assertTrue(full.equals(loaded.get(FishingBait.LOADED_BAIT)), "Crafting must load the right bait with 16 charges");
		ItemStack stripped = loaded.copy();
		stripped.remove(FishingBait.LOADED_BAIT);
		helper.assertTrue(ItemStack.matches(rod, stripped), "Loading must preserve the rod's name, damage and enchantment");
		var ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
		ItemStack restored = ItemStack.CODEC.parse(ops, ItemStack.CODEC.encodeStart(ops, loaded).getOrThrow()).getOrThrow();
		helper.assertTrue(ItemStack.matches(loaded, restored), "The bait must survive save and load");
		for (int charges = 15; charges >= 1; charges--) {
			helper.assertTrue(FishingBait.consume(loaded) == type && new FishingBait(type, charges).equals(loaded.get(FishingBait.LOADED_BAIT)),
				"Each use must spend exactly one charge");
		}
		helper.assertTrue(FishingBait.consume(loaded) == type && ItemStack.matches(rod, loaded), "The final charge must still supply the bait effect");
	}

	private static ItemStack craft(GameTestHelper helper, ItemStack first, ItemStack second) {
		CraftingInput input = CraftingInput.of(2, 1, List.of(first, second));
		var level = helper.getLevel();
		return level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, level).orElseThrow().value().assemble(input);
	}

	private static void assertRichFish(GameTestHelper helper, ItemStack fish, int nutrition) {
		var food = fish.get(DataComponents.FOOD);
		var original = fish.getPrototype().get(DataComponents.FOOD);
		helper.assertTrue(fish.has(FishingBait.RICH_FISH) && food.nutrition() == nutrition
			&& close(food.saturation(), original.saturation() * 1.3F), "Rich fish should grant the new nutrition and 30% more saturation");
	}

	private static boolean close(float first, float second) {
		return Math.abs(first - second) < 0.0001F;
	}
}
