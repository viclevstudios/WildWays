package com.viclev.wildways;

import java.util.Collections;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;

/** Checks storage-block registration and both packing and unpacking recipes. */
public class VolcaniteGameTests {
	@GameTest
	public void storageBlocksPackAndUnpackWithoutMaterialLoss(GameTestHelper helper) {
		helper.assertTrue(ModBlocks.RAW_VOLCANITE_BLOCK.defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE)
			&& ModBlocks.RAW_VOLCANITE_BLOCK.defaultBlockState().is(BlockTags.NEEDS_IRON_TOOL),
			"Raw Volcanite Block must require an iron-tier pickaxe");
		helper.assertTrue(ModBlocks.VOLCANITE_BLOCK.defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE)
			&& ModBlocks.VOLCANITE_BLOCK.defaultBlockState().is(BlockTags.NEEDS_IRON_TOOL),
			"Volcanite Block must require an iron-tier pickaxe");
		this.assertRecipe(helper, CraftingInput.of(3, 3, Collections.nCopies(9, new ItemStack(ModItems.RAW_VOLCANITE))),
			ModBlocks.RAW_VOLCANITE_BLOCK.asItem(), 1);
		this.assertRecipe(helper, CraftingInput.of(1, 1, List.of(new ItemStack(ModBlocks.RAW_VOLCANITE_BLOCK))),
			ModItems.RAW_VOLCANITE, 9);
		this.assertRecipe(helper, CraftingInput.of(3, 3, Collections.nCopies(9, new ItemStack(ModItems.VOLCANITE))),
			ModBlocks.VOLCANITE_BLOCK.asItem(), 1);
		this.assertRecipe(helper, CraftingInput.of(1, 1, List.of(new ItemStack(ModBlocks.VOLCANITE_BLOCK))),
			ModItems.VOLCANITE, 9);
		helper.succeed();
	}

	private void assertRecipe(GameTestHelper helper, CraftingInput input, Item expected, int count) {
		var recipe = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
		helper.assertTrue(recipe.isPresent(), "Storage recipe must be loaded");
		ItemStack result = recipe.orElseThrow().value().assemble(input);
		helper.assertTrue(result.is(expected) && result.getCount() == count,
			"Storage recipe must give the correct item count");
	}
}
