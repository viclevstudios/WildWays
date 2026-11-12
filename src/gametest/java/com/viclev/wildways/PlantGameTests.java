package com.viclev.wildways;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class PlantGameTests {
	@GameTest
	public void foodGuideCreditsPlantingTillingAndSnifferPickups(GameTestHelper helper) {
		var player = helper.makeMockServerPlayerInLevel();
		var level = helper.getLevel();
		var soil = new BlockPos(1, 1, 1);
		var worldSoil = helper.absolutePos(soil);
		var hit = new BlockHitResult(Vec3.atCenterOf(worldSoil), Direction.UP, worldSoil, false);
		helper.setBlock(soil, PlantBlocks.COMPOST);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_HOE));
		player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.PITCHER_POD));
		player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
		var progress = QuestBookItem.readProgress(player);
		for (String id : List.of("plant_seed", "till_fast_farmland", "till_compost", "pitcher_irrigation")) {
			helper.assertTrue(progress.get(Wildways.id("quests/" + id)).done(), "Expected quest credit for " + id);
		}

		var cycadQuest = Wildways.id("quests/find_cycad");
		var pos = helper.absolutePos(new BlockPos(2, 2, 1));
		var looseSeed = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(PlantBlocks.CYCAD));
		looseSeed.playerTouch(player);
		helper.assertFalse(QuestBookItem.readProgress(player).get(cycadQuest).done(), "A cycad without a Sniffer source must not count");
		var sniffer = helper.spawn(EntityTypes.SNIFFER, new BlockPos(2, 2, 1));
		var dugSeed = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(PlantBlocks.CYCAD));
		dugSeed.setThrower(sniffer);
		dugSeed.playerTouch(player);
		helper.assertTrue(QuestBookItem.readProgress(player).get(cycadQuest).done(), "Picking up a cycad from a Sniffer must count");
		helper.succeed();
	}

	@GameTest
	public void foodGuideDistinguishesGoldRipeningFromOrdinaryHarvestAndStew(GameTestHelper helper) {
		var player = helper.makeMockServerPlayerInLevel();
		var level = helper.getLevel();
		var crop = new BlockPos(1, 2, 1);
		var worldCrop = helper.absolutePos(crop);
		var normal = Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, CropBlock.MAX_AGE);
		var discovered = normal.setValue(PlantGrowth.QUALITY, PlantGrowth.LEVEL_TWO_SEEDS);
		var ripen = Wildways.id("quests/gold_ripen");
		var compost = Wildways.id("quests/gold_ripen_compost");
		var stew = Wildways.id("quests/eat_gold_ripened_stew");
		helper.setBlock(crop.below(), PlantBlocks.COMPOST_FARMLAND);
		Block.getDrops(normal, level, worldCrop, null, player, ItemStack.EMPTY);
		CriteriaTriggers.CONSUME_ITEM.trigger(player, new ItemStack(Items.RABBIT_STEW));
		var progress = QuestBookItem.readProgress(player);
		helper.assertFalse(progress.get(ripen).done() || progress.get(compost).done() || progress.get(stew).done(),
			"Ordinary harvests and stew must not count");
		Block.getDrops(discovered, level, worldCrop, null, player, ItemStack.EMPTY);
		CriteriaTriggers.CONSUME_ITEM.trigger(player, PlantQuality.mark(new ItemStack(Items.RABBIT_STEW)));
		progress = QuestBookItem.readProgress(player);
		helper.assertTrue(progress.get(ripen).done() && progress.get(compost).done() && progress.get(stew).done(),
			"Harvesting naturally ripened stock on compost and eating gold-ripened stew must count");
		helper.succeed();
	}

	@GameTest
	public void torchflowersCanBePlantedAndGrowInDarkness(GameTestHelper helper) {
		BlockPos soil = new BlockPos(1, 1, 1);
		BlockPos crop = soil.above();
		for (int x = 0; x <= 2; x++) {
			for (int z = 0; z <= 2; z++) {
				for (int y = 1; y <= 3; y++) {
					if (x != 1 || z != 1 || y == 3) {
						helper.setBlock(x, y, z, Blocks.STONE);
					}
				}
			}
		}
		helper.setBlock(soil, Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE, 7));
		helper.runAfterDelay(10, () -> {
			var level = helper.getLevel();
			var worldCrop = helper.absolutePos(crop);
			helper.assertTrue(level.getRawBrightness(worldCrop, 0) < 8, "The test plot should be dark");
			helper.assertFalse(Blocks.WHEAT.defaultBlockState().canSurvive(level, worldCrop), "Ordinary wheat still needs light");
			var player = helper.makeMockPlayer(GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.TORCHFLOWER_SEEDS));
			var worldSoil = helper.absolutePos(soil);
			helper.useBlock(soil, player, new BlockHitResult(Vec3.atCenterOf(worldSoil), Direction.UP, worldSoil, false));
			helper.assertBlockPresent(Blocks.TORCHFLOWER_CROP, crop);
			RandomSource random = RandomSource.create(123);
			for (int tick = 0; tick < 512 && helper.getBlockState(crop).is(Blocks.TORCHFLOWER_CROP); tick++) {
				helper.getBlockState(crop).randomTick(level, worldCrop, random);
			}
			helper.assertBlockPresent(Blocks.TORCHFLOWER, crop);
			helper.succeed();
		});
	}

	@GameTest
	public void commandItemsMatchHarvestedGoldRipenedStacks(GameTestHelper helper) throws CommandSyntaxException {
		ItemParser parser = new ItemParser(helper.getLevel().registryAccess());
		var seeds = parser.parse(new StringReader("minecraft:wheat_seeds[wildways:level_two_crop={}]")).createItemStack(16);
		var carrots = parser.parse(new StringReader(
			"minecraft:carrot[wildways:level_two_crop={},minecraft:food={nutrition:4,saturation:5.0}]")).createItemStack(16);
		helper.assertTrue(ItemStack.matches(seeds, PlantQuality.mark(new ItemStack(Items.WHEAT_SEEDS, 16))),
			"The seed command should create the same stack as a harvest");
		helper.assertTrue(ItemStack.matches(carrots, PlantQuality.mark(new ItemStack(Items.CARROT, 16))),
			"The carrot command should create the same stack and nutrition as a harvest");
		helper.succeed();
	}

	@GameTest
	public void compostBatchRejectsMixingAndProducesCompost(GameTestHelper helper) {
		BlockPos pos = new BlockPos(1, 1, 1);
		var level = helper.getLevel();
		var worldPos = helper.absolutePos(pos);
		var composter = (ComposterBlock) Blocks.COMPOSTER;
		helper.setBlock(pos, composter);
		var player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT_SEEDS));
		helper.useBlock(pos, player);
		var ordinary = helper.getBlockState(pos);
		helper.assertTrue(ordinary.getValue(ComposterBlock.LEVEL) == 1, "The first vanilla input should fill one layer");
		ItemStack flower = new ItemStack(Items.TORCHFLOWER);
		ComposterBlock.insertItem(player, ordinary, level, flower, worldPos);
		helper.assertTrue(flower.getCount() == 1 && helper.getBlockState(pos) == ordinary, "An ordinary batch must reject Sniffer plants");

		helper.setBlock(pos, composter);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.PITCHER_PLANT));
		helper.useBlock(pos, player);
		var special = helper.getBlockState(pos);
		helper.assertTrue(PlantComposting.isSnifferBatch(special), "A Sniffer flower should start a compost batch");
		var input = composter.getContainer(special, level, worldPos);
		helper.assertFalse(input.canPlaceItemThroughFace(0, new ItemStack(Items.WHEAT_SEEDS), Direction.UP),
			"Hoppers must reject ordinary plants in a compost batch");
		helper.assertTrue(input.canPlaceItemThroughFace(0, flower, Direction.UP), "Sniffer plants should mix with each other");

		var ready = special.setValue(ComposterBlock.LEVEL, 8);
		helper.setBlock(pos, ready);
		var output = composter.getContainer(ready, level, worldPos);
		helper.assertTrue(output.getItem(0).is(PlantBlocks.COMPOST_ITEM)
			&& output.canTakeItemThroughFace(0, output.getItem(0), Direction.DOWN), "Hoppers should extract compost");
		ComposterBlock.extractProduce(player, ready, level, worldPos);
		helper.assertFalse(PlantComposting.isSnifferBatch(helper.getBlockState(pos)), "Harvesting must reset the batch");
		helper.assertItemEntityPresent(PlantBlocks.COMPOST_ITEM, pos, 2);
		helper.succeed();
	}

	@GameTest
	public void compostSupportsCropsAndPitcherHarvestReturnsExactlyTwoPods(GameTestHelper helper) {
		BlockPos soil = new BlockPos(1, 1, 1);
		BlockPos crop = soil.above();
		helper.setBlock(soil, PlantBlocks.COMPOST);
		var player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_HOE));
		var worldSoil = helper.absolutePos(soil);
		var top = new BlockHitResult(Vec3.atCenterOf(worldSoil), Direction.UP, worldSoil, false);
		helper.useBlock(soil, player, top);
		helper.assertBlockPresent(PlantBlocks.COMPOST_FARMLAND, soil);
		helper.assertTrue(PlantGrowth.growthMultiplier(helper.getLevel(), helper.absolutePos(crop)) == 1.4F,
			"The compost plot should remember the diamond hoe");

		var pitcher = Blocks.PITCHER_CROP.defaultBlockState().setValue(PitcherCropBlock.AGE, 4);
		var pitcherDrops = Block.getDrops(pitcher, helper.getLevel(), helper.absolutePos(crop), null);
		helper.assertTrue(pitcherDrops.size() == 1 && count(pitcherDrops, Items.PITCHER_POD) == 2,
			"Ripe pitcher crops on compost must drop exactly two pods");
		helper.setBlock(crop, Blocks.PITCHER_CROP.defaultBlockState());
		helper.randomTick(soil);
		helper.assertTrue(helper.getBlockState(soil).getValue(FarmlandBlock.MOISTURE) == 7, "Planted pitcher pods should hydrate compost");
		helper.assertTrue(Blocks.TORCHFLOWER.defaultBlockState().getLightEmission() == 9
			&& Blocks.POTTED_TORCHFLOWER.defaultBlockState().getLightEmission() == 9, "Both forms of torchflower should emit light");
		helper.succeed();
	}

	@GameTest
	public void onlyNaturalRipeningCanDiscoverGoldRipenedSeeds(GameTestHelper helper) {
		BlockPos crop = new BlockPos(1, 2, 1);
		var level = helper.getLevel();
		var worldCrop = helper.absolutePos(crop);
		helper.setBlock(crop.below(), PlantBlocks.COMPOST_FARMLAND);
		helper.setBlock(crop.east().below(), Blocks.FARMLAND);
		helper.setBlock(crop.east(), Blocks.TORCHFLOWER);
		RandomSource successfulRoll = new LegacyRandomSource(0) {
			@Override
			public int next(int bits) {
				return 0;
			}
		};

		for (var immature : List.of(
			Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 6),
			Blocks.MELON_STEM.defaultBlockState().setValue(StemBlock.AGE, 6),
			Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 2)
		)) {
			helper.setBlock(crop, immature);
			immature.randomTick(level, worldCrop, successfulRoll);
			helper.assertTrue(helper.getBlockState(crop).getValue(PlantGrowth.QUALITY) == PlantGrowth.LEVEL_TWO_SEEDS,
				"A successful natural roll should produce Gold-Ripened planting stock");

			for (int attempt = 0; attempt < 64; attempt++) {
				helper.setBlock(crop, immature);
				helper.assertTrue(BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), level, worldCrop),
					"Bone meal should still finish ripening the crop");
				var ripe = helper.getBlockState(crop);
				helper.assertTrue(PlantGrowth.isMature(ripe) && ripe.getValue(PlantGrowth.QUALITY) == PlantGrowth.NORMAL,
					"Bone meal should never discover Gold-Ripened planting stock");
			}
		}
		helper.spawn(EntityTypes.BEE, crop.above());
		helper.succeed();
	}

	@GameTest
	public void ripeWheatKeepsQualityAndUsesCompostYield(GameTestHelper helper) {
		BlockPos crop = new BlockPos(1, 2, 1);
		var level = helper.getLevel();
		helper.setBlock(crop.below(), PlantBlocks.COMPOST_FARMLAND);
		var player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, PlantQuality.mark(new ItemStack(Items.WHEAT_SEEDS)));
		var worldSoil = helper.absolutePos(crop.below());
		helper.useBlock(crop.below(), player, new BlockHitResult(Vec3.atCenterOf(worldSoil), Direction.UP, worldSoil, false));
		helper.assertBlockPresent(Blocks.WHEAT, crop);
		var boneMeal = new ItemStack(Items.BONE_MEAL, 4);
		while (!boneMeal.isEmpty() && BoneMealItem.growCrop(boneMeal, level, helper.absolutePos(crop))) {
			// Four doses cover even the smallest vanilla growth increments.
		}
		var wheat = helper.getBlockState(crop);
		helper.assertTrue(PlantGrowth.isMature(wheat) && wheat.getValue(PlantGrowth.QUALITY) == PlantGrowth.LEVEL_TWO,
			"Bone meal should mature Gold-Ripened wheat and preserve its level");
		for (int roll = 0; roll < 24; roll++) {
			var drops = Block.getDrops(wheat, level, helper.absolutePos(crop), null);
			int grains = count(drops, Items.WHEAT);
			int seeds = count(drops, Items.WHEAT_SEEDS);
			helper.assertTrue(grains >= 1 && grains <= 2 && seeds >= 1 && seeds <= 4, "Compost wheat yield must stay within its base ranges");
			helper.assertTrue(drops.stream().allMatch(stack -> stack.has(PlantQuality.LEVEL_TWO)),
				"Ripe level-two wheat should keep both grain and seeds at level two");
		}
		helper.succeed();
	}

	@GameTest
	public void melonsInheritTheirStemsQualityAndSoil(GameTestHelper helper) {
		BlockPos stem = new BlockPos(1, 2, 1);
		var level = helper.getLevel();
		helper.setBlock(stem.below(), PlantBlocks.COMPOST_FARMLAND);
		for (Direction side : Direction.Plane.HORIZONTAL) {
			helper.setBlock(stem.below().relative(side), Blocks.DIRT);
		}
		helper.setBlock(stem, Blocks.MELON_STEM.defaultBlockState().setValue(StemBlock.AGE, 7)
			.setValue(PlantGrowth.QUALITY, PlantGrowth.LEVEL_TWO));
		RandomSource random = RandomSource.create(123);
		for (int tick = 0; tick < 512 && helper.getBlockState(stem).is(Blocks.MELON_STEM); tick++) {
			helper.getBlockState(stem).randomTick(level, helper.absolutePos(stem), random);
		}
		helper.assertBlockPresent(Blocks.ATTACHED_MELON_STEM, stem);
		for (Direction side : Direction.Plane.HORIZONTAL) {
			BlockPos fruit = stem.relative(side);
			var melon = helper.getBlockState(fruit);
			if (melon.is(Blocks.MELON)) {
				helper.assertTrue(melon.getValue(PlantGrowth.QUALITY) == PlantGrowth.LEVEL_TWO
					&& melon.getValue(PlantGrowth.COMPOST_GROWN) == 1, "The melon must keep the level and compost yield");
				var drops = Block.getDrops(melon, level, helper.absolutePos(fruit), null);
				int slices = count(drops, Items.MELON_SLICE);
				helper.assertTrue(slices >= 4 && slices <= 9 && drops.stream().allMatch(stack -> stack.has(PlantQuality.LEVEL_TWO)),
					"The melon should yield 4–9 level-two slices");
				helper.succeed();
				return;
			}
		}
		helper.fail("A mature stem should grow a melon");
	}

	@GameTest
	public void recipesKeepQualityOnlyWithFullyUpgradedCropIngredients(GameTestHelper helper) {
		var level = helper.getLevel();
		var wheat = PlantQuality.mark(new ItemStack(Items.WHEAT));
		var bread = CraftingInput.of(3, 1, List.of(wheat, wheat.copy(), wheat.copy()));
		var mixed = CraftingInput.of(3, 1, List.of(wheat, wheat.copy(), new ItemStack(Items.WHEAT)));
		var recipe = level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, bread, level).orElseThrow().value();
		var finished = recipe.assemble(bread);
		var food = finished.get(DataComponents.FOOD);
		helper.assertTrue(finished.has(PlantQuality.LEVEL_TWO) && food != null && food.nutrition() == 6 && food.saturation() == 8,
			"Level-two bread should use the specified food values");
		helper.assertFalse(recipe.assemble(mixed).has(PlantQuality.LEVEL_TWO), "Mixed wheat should still craft ordinary bread");

		var potato = new SingleRecipeInput(PlantQuality.mark(new ItemStack(Items.POTATO)));
		var baked = level.recipeAccess().getRecipeFor(RecipeType.SMELTING, potato, level).orElseThrow().value().assemble(potato);
		helper.assertTrue(baked.is(Items.BAKED_POTATO) && baked.has(PlantQuality.LEVEL_TWO), "Baking should keep the potato's level");

		var slice = PlantQuality.mark(new ItemStack(Items.MELON_SLICE));
		var melonSeed = CraftingInput.of(1, 1, List.of(slice));
		var seed = level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, melonSeed, level).orElseThrow().value().assemble(melonSeed);
		helper.assertTrue(seed.is(Items.MELON_SEEDS) && seed.has(PlantQuality.LEVEL_TWO), "Crafted melon seeds should inherit the slice's level");
		helper.succeed();
	}

	private static int count(List<ItemStack> drops, Item item) {
		return drops.stream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
	}
}
