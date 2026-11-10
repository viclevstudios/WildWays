package com.viclev.wildways;

import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public class QuestGuideGameTests {
	private static final ResourceKey<Recipe<?>> BOOK_RECIPE = ResourceKey.create(Registries.RECIPE, Wildways.id("quest_book"));

	@GameTest
	public void firstJoinGivesOneBookAndUnlocksItsRecipe(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		helper.assertTrue(player.getInventory().countItem(ModItems.QUEST_BOOK) == 1, "Expected a starter quest book");
		helper.assertTrue(player.getRecipeBook().contains(BOOK_RECIPE), "Expected the replacement recipe to be unlocked");
		player.getInventory().clearContent();
		QuestBookItem.giveStarterBook(player);
		helper.assertTrue(player.getInventory().countItem(ModItems.QUEST_BOOK) == 0, "Rejoining should not grant a replacement");
		helper.succeed();
	}

	@GameTest
	public void recipeAcceptsMixedLeaves(GameTestHelper helper) {
		CraftingInput input = CraftingInput.of(3, 3, List.of(
			new ItemStack(Items.IRON_INGOT), new ItemStack(Items.OAK_LEAVES), new ItemStack(Items.IRON_INGOT),
			new ItemStack(Items.BIRCH_LEAVES), new ItemStack(Items.BOOK), new ItemStack(Items.SPRUCE_LEAVES),
			new ItemStack(Items.IRON_INGOT), new ItemStack(Items.AZALEA_LEAVES), new ItemStack(Items.IRON_INGOT)
		));
		var recipe = helper.getLevel().getServer().getRecipeManager()
			.getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel(), BOOK_RECIPE).orElseThrow();
		helper.assertTrue(recipe.id().equals(BOOK_RECIPE), "Expected the quest book recipe to match");
		helper.assertTrue(recipe.value().assemble(input).is(ModItems.QUEST_BOOK), "Expected a quest book result");
		helper.succeed();
	}

	@GameTest
	public void questProgressCountsIndependentStepsPerPlayer(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		ServerPlayer other = helper.makeMockServerPlayerInLevel();
		var nest = Wildways.id("quests/nest");
		var shell = Wildways.id("quests/shell");
		var unease = Wildways.id("quests/unease");
		ItemStack nestStack = new ItemStack(ModBlocks.ENDERMITE_BOX);
		player.getInventory().add(nestStack.copy());
		CriteriaTriggers.INVENTORY_CHANGED.trigger(player, player.getInventory(), nestStack);
		CriteriaTriggers.BREWED_POTION.trigger(player, new PotionContents(ModPotions.STRONG_UNEASE));

		var progress = QuestBookItem.readProgress(player);
		int totalQuests = QuestGuide.GROUPS.stream().mapToInt(group -> group.quests().size()).sum();
		helper.assertTrue(progress.size() == totalQuests, "Expected a loaded advancement for every quest");
		helper.assertTrue(progress.get(nest).done() && !progress.get(shell).done(), "A later quest should count first");
		helper.assertTrue(progress.get(unease).equals(new QuestGuide.Progress(1, 2)), "Expected one brewing step out of two");
		helper.assertTrue(!QuestBookItem.readProgress(other).get(nest).done(), "Quest progress must be per player");

		CriteriaTriggers.BREWED_POTION.trigger(player, new PotionContents(ModPotions.UNEASE));
		helper.assertTrue(QuestBookItem.readProgress(player).get(unease).done(), "Both steps should count in reverse order");
		helper.succeed();
	}
}
