package com.viclev.wildways;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class PortalEyeGameTests {
	@GameTest
	public void opensWithTwelveDifferentEyesFromTheSameRing(GameTestHelper helper) {
		BlockPos center = new BlockPos(2, 2, 2);
		List<BlockPos> frames = new ArrayList<>();
		for (Direction outward : Direction.Plane.HORIZONTAL) {
			for (int offset = -1; offset <= 1; offset++) {
				BlockPos frame = center.relative(outward, 2).relative(outward.getClockWise(), offset);
				helper.setBlock(frame, Blocks.END_PORTAL_FRAME.defaultBlockState()
					.setValue(EndPortalFrameBlock.FACING, outward.getOpposite()));
				frames.add(frame);
			}
		}

		var player = helper.makeMockServerPlayerInLevel();
		var activationQuest = Wildways.id("quests/activate_end_portal");
		BlockPos strayFrame = center.above();
		helper.setBlock(strayFrame, Blocks.END_PORTAL_FRAME);
		useEye(helper, player, strayFrame, ModItems.EYE_OF_SULFUR);
		List<PortalEyeItem> selected = ModItems.PORTAL_EYES.stream()
			.filter(eye -> eye != ModItems.EYE_OF_THE_TIGER && eye != ModItems.ENCHANTED_EYE)
			.toList();
		helper.assertValueEqual(selected.size(), 12, "number of selected eyes");
		for (int i = 0; i < frames.size() - 1; i++) {
			useEye(helper, player, frames.get(i), selected.get(i));
		}
		helper.assertBlockNotPresent(Blocks.END_PORTAL, center);
		helper.assertTrue(!QuestBookItem.readProgress(player).get(activationQuest).done(), "An incomplete ring should not count");

		BlockPos lastFrame = frames.getLast();
		useEye(helper, player, lastFrame, ModItems.EYE_OF_SULFUR);
		useEye(helper, player, lastFrame, ModItems.EYE_OF_VOLCANITE);
		useEye(helper, player, lastFrame, ModItems.ENCHANTED_EYE);
		useEye(helper, player, lastFrame, Items.ENDER_EYE);
		helper.assertBlockProperty(lastFrame, EndPortalFrameBlock.HAS_EYE, false);
		useEye(helper, player, lastFrame, ModItems.ATTUNED_EYE_OF_VOLCANITE);
		helper.assertBlockProperty(lastFrame, PortalEyeStates.EYE_TYPE, 14);
		helper.assertBlockPresent(Blocks.END_PORTAL, center);
		helper.assertTrue(QuestBookItem.readProgress(player).get(activationQuest).done(), "Opening the portal should count");
		helper.assertTrue(!QuestBookItem.readProgress(player).get(Wildways.id("quests/collect_portal_eyes")).done(),
			"Portal activation should count independently of the collection quest");

		AttackBlockCallback.EVENT.invoker().interact(player, helper.getLevel(), InteractionHand.MAIN_HAND,
			helper.absolutePos(lastFrame), Direction.UP);
		helper.assertBlockProperty(lastFrame, EndPortalFrameBlock.HAS_EYE, false);
		helper.assertBlockNotPresent(Blocks.END_PORTAL, center);
		helper.assertTrue(player.getInventory().contains(new ItemStack(ModItems.ATTUNED_EYE_OF_VOLCANITE)),
			"Removing an eye should return the attuned eye");
		helper.succeed();
	}

	@GameTest(maxTicks = 80)
	public void craftsAndTransformsEyesInSulfurWaterAndFire(GameTestHelper helper) {
		ItemStack ingot = new ItemStack(ModItems.VOLCANITE);
		ItemStack enderEye = new ItemStack(Items.ENDER_EYE);
		ItemStack empty = ItemStack.EMPTY;
		CraftingInput input = CraftingInput.of(3, 3, List.of(
			empty, ingot, empty,
			ingot, enderEye, ingot,
			empty, ingot, empty
		));
		var recipe = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
		helper.assertTrue(recipe.isPresent(), "Four Volcanite Ingots in a cross around an Eye of Ender should match the recipe");
		ItemStack craftedEye = recipe.orElseThrow().value().assemble(input);
		helper.assertTrue(craftedEye.is(ModItems.EYE_OF_VOLCANITE) && craftedEye.getCount() == 1,
			"The recipe should yield one Eye of Volcanite");
		CraftingInput corners = CraftingInput.of(3, 3, List.of(
			ingot, empty, ingot,
			empty, enderEye, empty,
			ingot, empty, ingot
		));
		helper.assertTrue(helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, corners, helper.getLevel()).isEmpty(),
			"Volcanite Ingots in the corners should not match the recipe");

		helper.setBlock(new BlockPos(1, 1, 1), Blocks.POTENT_SULFUR);
		helper.setBlock(new BlockPos(1, 2, 1), Blocks.WATER);
		helper.setBlock(new BlockPos(2, 2, 2), Blocks.WATER);
		helper.setBlock(new BlockPos(3, 1, 3), Blocks.NETHERRACK);
		helper.setBlock(new BlockPos(3, 2, 3), Blocks.FIRE);
		var sulfurEye = helper.spawnItem(Items.ENDER_EYE, 1.5F, 2.2F, 1.5F);
		var ordinaryEye = helper.spawnItem(Items.ENDER_EYE, 2.5F, 2.2F, 2.5F);
		var volcaniteEye = helper.spawnItem(craftedEye.getItem(), 3.5F, 2.2F, 3.5F);
		volcaniteEye.setDeltaMovement(Vec3.ZERO);
		double fireY = helper.absolutePos(new BlockPos(3, 2, 3)).getY();

		helper.runAfterDelay(5, () -> {
			helper.assertTrue(sulfurEye.getItem().is(ModItems.EYE_OF_SULFUR), "Potent sulfur water should convert an Eye of Ender");
			helper.assertTrue(ordinaryEye.getItem().is(Items.ENDER_EYE), "Ordinary water should leave an Eye of Ender intact");
		});
		helper.runAfterDelay(9, () -> {
			helper.assertFalse(EyeTransformations.isVolcaniteRising(volcaniteEye), "The eye should keep falling normally during the first half second");
			helper.assertTrue(volcaniteEye.getY() < fireY + 0.15, "The eye should settle into the fire before rising");
		});
		helper.runAfterDelay(30, () -> {
			helper.assertTrue(EyeTransformations.isVolcaniteRising(volcaniteEye), "The eye should rise after the first half second");
			helper.assertTrue(volcaniteEye.getItem().is(ModItems.EYE_OF_VOLCANITE), "Volcanite eye should remain unattuned during the ascent");
			helper.assertTrue(volcaniteEye.getY() > fireY + 0.5 && volcaniteEye.getY() < fireY + 1.5,
				"Volcanite eye should be about halfway through its ascent");
		});
		helper.succeedWhen(() -> {
			helper.assertTrue(volcaniteEye.isAlive() && volcaniteEye.getItem().is(ModItems.ATTUNED_EYE_OF_VOLCANITE),
				"Fire should attune the Eye of Volcanite after the ascent");
			helper.assertTrue(Math.abs(volcaniteEye.getY() - fireY - 2) < 0.15,
				"Volcanite eye should attune about two blocks above its starting position");
		});
	}

	private static void useEye(GameTestHelper helper, Player player, BlockPos frame, Item eye) {
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(eye));
		helper.useBlock(frame, player);
	}
}
