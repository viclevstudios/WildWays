package com.viclev.wildways;

import java.util.List;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WitherSkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class QuestGuideGameTests {
	private static final ResourceKey<Recipe<?>> BOOK_RECIPE = ResourceKey.create(Registries.RECIPE, Wildways.id("quest_book"));
	private static final ResourceKey<Recipe<?>> NEST_RECIPE = ResourceKey.create(Registries.RECIPE, Wildways.id("endermite_box"));

	@GameTest
	public void questRewardsArePerCompletionEvenForSharedNodes(GameTestHelper helper) {
		var player = helper.makeMockServerPlayerInLevel();
		var quest = helper.getLevel().getServer().getAdvancements().get(Wildways.id("quests/endermite_eye"));
		var progress = player.getAdvancements().getOrStartProgress(quest);
		int xpBefore = player.totalExperience;
		var criterion = progress.getRemainingCriteria().iterator().next();

		player.getAdvancements().award(quest, criterion);
		helper.assertTrue(player.totalExperience == xpBefore + 25, "The eye shared by two groups should award 25 XP once");
		player.getAdvancements().revoke(quest, criterion);
		player.getAdvancements().award(quest, criterion);
		helper.assertTrue(player.totalExperience == xpBefore + 25, "Re-completing an already rewarded quest must not award more XP");

		var overrides = new QuestRewards.Settings(40, java.util.Map.of("endermite_eye", 0, "nest", 75));
		helper.assertTrue(overrides.xpFor("endermite_eye") == 0 && overrides.xpFor("nest") == 75 && overrides.xpFor("other") == 40,
			"Configured XP amounts should support overrides, zero and a default");
		helper.succeed();
	}

	@GameTest
	public void witherGuideRequiresAllSkullQuestsAndTheMixedRitual(GameTestHelper helper) {
		var player = helper.makeMockServerPlayerInLevel();
		var centerSkull = new BlockPos(1, 3, 1);
		var worldSkull = helper.absolutePos(centerSkull);
		player.snapTo(worldSkull.getX(), worldSkull.getY(), worldSkull.getZ(), 0, 0);
		var summonQuest = Wildways.id("quests/summon_wither");
		var regular = Blocks.WITHER_SKELETON_SKULL.defaultBlockState();
		var sculk = WitherSkulls.SCULK.skull().defaultBlockState();
		var gold = WitherSkulls.GOLD.skull().defaultBlockState();

		summonWither(helper, centerSkull, sculk, gold);
		helper.assertFalse(QuestBookItem.readProgress(player).get(summonQuest).done(), "A mixed summon without the skull quests must not count");

		for (var skull : List.of(Items.WITHER_SKELETON_SKULL, WitherSkulls.SCULK.item(), WitherSkulls.GOLD.item())) {
			CriteriaTriggers.INVENTORY_CHANGED.trigger(player, player.getInventory(), new ItemStack(skull));
		}
		summonWither(helper, centerSkull, regular, regular);
		helper.assertFalse(QuestBookItem.readProgress(player).get(summonQuest).done(), "Three ordinary skulls must not count for the mixed ritual");

		summonWither(helper, centerSkull, gold, sculk);
		helper.assertTrue(QuestBookItem.readProgress(player).get(summonQuest).done(), "A mixed summon with all three skull quests must count");
		helper.succeed();
	}

	private static void summonWither(GameTestHelper helper, BlockPos centerSkull, BlockState left, BlockState right) {
		var soulSand = Blocks.SOUL_SAND;
		helper.setBlock(centerSkull.below(2), soulSand);
		for (Direction direction : List.of(Direction.EAST, Direction.WEST)) {
			helper.setBlock(centerSkull.relative(direction).below(), soulSand);
			helper.setBlock(centerSkull.relative(direction).below(2), Blocks.AIR);
		}
		helper.setBlock(centerSkull.below(), soulSand);
		helper.setBlock(centerSkull.west(), left);
		helper.setBlock(centerSkull.east(), right);
		helper.setBlock(centerSkull, Blocks.WITHER_SKELETON_SKULL);
		var level = helper.getLevel();
		var skull = helper.absolutePos(centerSkull);
		WitherSkullBlock.checkSpawn(level, skull);
		var withers = level.getEntitiesOfClass(WitherBoss.class, new AABB(skull).inflate(4.0));
		helper.assertTrue(withers.size() == 1, "The ritual should spawn one Wither");
		withers.getFirst().discard();
	}

	@GameTest
	public void animalWeightsFoodQuestsFollowTheInstalledMod(GameTestHelper helper) throws ReflectiveOperationException {
		var player = helper.makeMockServerPlayerInLevel();
		var pasture = Wildways.id("quests/perfect_habitat");
		var heavy = Wildways.id("quests/hunt_heavy_livestock");
		if (!FabricLoader.getInstance().isModLoaded("animalweights")) {
			var progress = QuestBookItem.readProgress(player);
			helper.assertFalse(progress.containsKey(pasture) || progress.containsKey(heavy), "Animal Weights quests must be absent");
			helper.succeed();
			return;
		}

		helper.assertTrue(AnimalWeightsQuests.isAvailable(), "The Animal Weights adapter must load");
		var level = helper.getLevel();
		var position = new BlockPos(1, 2, 1);
		var cow = helper.spawn(EntityTypes.COW, position);
		cow.setNoAi(true);
		Class.forName("com.leclowndu93150.animalweights.AnimalWeightsRules")
			.getMethod("markEngaged", Animal.class).invoke(null, cow);
		var glassId = Identifier.fromNamespaceAndPath("animalweights", "magnifying_glass");
		var glass = BuiltInRegistries.ITEM.getOptional(glassId).orElseThrow();
		CriteriaTriggers.RECIPE_CRAFTED.trigger(player, ResourceKey.create(Registries.RECIPE, glassId), List.of());
		helper.assertTrue(QuestBookItem.readProgress(player).get(Wildways.id("quests/craft_magnifying_glass")).done(),
			"Crafting the Animal Weights recipe must count");
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(glass));
		UseEntityCallback.EVENT.invoker().interact(player, level, InteractionHand.MAIN_HAND, cow, null);
		helper.assertFalse(QuestBookItem.readProgress(player).get(pasture).done(), "Inspecting an incomplete pasture must not count");

		helper.setBlock(position.below(), Blocks.GRASS_BLOCK);
		helper.setBlock(position.east().below(), Blocks.WATER);
		helper.setBlock(position.west().below(), Blocks.GLOWSTONE);
		Class.forName("com.leclowndu93150.animalweights.WeightAttachment")
			.getMethod("setWeight", Animal.class, int.class).invoke(null, cow, 8);
		helper.succeedWhen(() -> {
			UseEntityCallback.EVENT.invoker().interact(player, level, InteractionHand.MAIN_HAND, cow, null);
			helper.assertTrue(QuestBookItem.readProgress(player).get(pasture).done(), "Inspecting a pasture that meets all four needs must count");
			cow.hurtServer(level, level.damageSources().playerAttack(player), 1000.0F);
			var progress = QuestBookItem.readProgress(player);
			helper.assertTrue(progress.get(heavy).done() && progress.get(Wildways.id("quests/hunt_livestock")).done(),
				"Killing livestock at weight eight must complete both hunting quests");
		});
	}

	@GameTest
	public void explorationDistinguishesMapsAndTrades(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		var anyMap = Wildways.id("quests/structure_map");
		var tradedMap = Wildways.id("quests/trade_structure_map");
		var iglooMap = Wildways.id("quests/igloo_map");
		var trader = helper.spawn(EntityTypes.WANDERING_TRADER, new BlockPos(1, 2, 1));
		var villager = helper.spawn(EntityTypes.VILLAGER, new BlockPos(2, 2, 1));
		var localMap = new ItemStack(Items.FILLED_MAP);
		var explorerMap = new ItemStack(Items.WOODLAND_MANSION_MAP);

		CriteriaTriggers.INVENTORY_CHANGED.trigger(player, player.getInventory(), localMap);
		CriteriaTriggers.TRADE.trigger(player, trader, localMap);
		CriteriaTriggers.TRADE.trigger(player, villager, explorerMap);
		var progress = QuestBookItem.readProgress(player);
		helper.assertFalse(progress.get(anyMap).done(), "An ordinary filled map must not count");
		helper.assertFalse(progress.get(tradedMap).done(), "Only a structure map bought from a Wandering Trader must count");

		CriteriaTriggers.INVENTORY_CHANGED.trigger(player, player.getInventory(), explorerMap);
		progress = QuestBookItem.readProgress(player);
		helper.assertTrue(progress.get(anyMap).done(), "Vanilla explorer maps must count");
		helper.assertFalse(progress.get(iglooMap).done(), "A mansion map must not complete the igloo quest");
		CriteriaTriggers.TRADE.trigger(player, trader, new ItemStack(ModItems.IGLOO_MAP));
		CriteriaTriggers.INVENTORY_CHANGED.trigger(player, player.getInventory(), new ItemStack(ModItems.IGLOO_MAP));
		progress = QuestBookItem.readProgress(player);
		helper.assertTrue(progress.get(tradedMap).done() && progress.get(iglooMap).done(), "Trading for and holding the correct map must count");
		helper.succeed();
	}

	@GameTest
	public void explorationReadsHeldInstrumentsAndTracksWornElytraLanding(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		var compass = Wildways.id("quests/compass");
		var clock = Wildways.id("quests/read_clock");
		var sensor = Wildways.id("quests/read_light_sensor");
		var landing = Wildways.id("quests/land_with_worn_elytra");
		player.getInventory().setItem(9, new ItemStack(Items.COMPASS));
		CriteriaTriggers.TICK.trigger(player);
		helper.assertFalse(QuestBookItem.readProgress(player).get(compass).done(), "A stowed compass must not count");
		player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.COMPASS));
		CriteriaTriggers.TICK.trigger(player);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CLOCK));
		CriteriaTriggers.TICK.trigger(player);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModBlocks.LIGHT_SENSOR));
		CriteriaTriggers.TICK.trigger(player);
		var progress = QuestBookItem.readProgress(player);
		helper.assertTrue(progress.get(compass).done() && progress.get(clock).done() && progress.get(sensor).done(),
			"Each held instrument must complete its reading quest");

		ItemStack elytra = new ItemStack(Items.ELYTRA);
		elytra.setDamageValue(elytra.getMaxDamage() - 1);
		player.setItemSlot(EquipmentSlot.CHEST, elytra);
		player.setOnGround(false);
		player.startFallFlying();
		player.aiStep();
		helper.assertFalse(QuestBookItem.readProgress(player).get(landing).done(), "Still gliding must not complete the landing quest");
		player.setOnGround(true);
		player.aiStep();
		helper.assertTrue(QuestBookItem.readProgress(player).get(landing).done(), "Touching down with worn Elytra must count");
		helper.succeed();
	}

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
		var chapterUnease = Wildways.id("quests/brew_unease");
		CriteriaTriggers.RECIPE_CRAFTED.trigger(player, NEST_RECIPE, List.of());
		CriteriaTriggers.BREWED_POTION.trigger(player, new PotionContents(ModPotions.STRONG_UNEASE));

		var progress = QuestBookItem.readProgress(player);
		long distinctAdvancements = QuestGuide.GROUPS.stream().flatMap(group -> group.quests().stream())
			.map(QuestGuide.Quest::advancement).distinct().count();
		helper.assertTrue(progress.size() == distinctAdvancements, "Expected a loaded advancement for every quest");
		helper.assertTrue(progress.get(nest).done() && !progress.get(shell).done(), "A later quest should count first");
		helper.assertTrue(progress.get(Wildways.id("quests/brew_level_two")).done(), "Strong Unease should count as a level II potion");
		helper.assertTrue(!progress.get(chapterUnease).done(), "The chapter requires the regular Potion of Unease");
		helper.assertTrue(!QuestBookItem.readProgress(other).get(nest).done(), "Quest progress must be per player");

		CriteriaTriggers.BREWED_POTION.trigger(player, new PotionContents(ModPotions.UNEASE));
		helper.assertTrue(QuestBookItem.readProgress(player).get(chapterUnease).done(), "Regular Unease should complete the chapter quest");
		helper.succeed();
	}

	@GameTest
	public void brewingQuestsRecognizeStacksBrewsAndDrinking(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		var stackQuest = Wildways.id("quests/stack_potions");
		var supremeBrew = Wildways.id("quests/brew_supreme");
		var fatalBrew = Wildways.id("quests/brew_fatal");
		var drinkQuest = Wildways.id("quests/drink_supreme");
		ItemStack potions = PotionContents.createItemStack(Items.SPLASH_POTION, Potions.SWIFTNESS);
		potions.setCount(7);
		CriteriaTriggers.INVENTORY_CHANGED.trigger(player, player.getInventory(), potions);
		helper.assertTrue(!QuestBookItem.readProgress(player).get(stackQuest).done(), "Seven potions should not count");
		potions.setCount(8);
		player.getInventory().add(potions.copy());
		CriteriaTriggers.INVENTORY_CHANGED.trigger(player, player.getInventory(), potions);
		helper.assertTrue(QuestBookItem.readProgress(player).get(stackQuest).done(), "Eight potions in one slot should count");

		ServerPlayer other = helper.makeMockServerPlayerInLevel();
		ItemStack water = PotionContents.createItemStack(Items.POTION, Potions.WATER);
		water.setCount(8);
		other.getInventory().add(water.copy());
		CriteriaTriggers.INVENTORY_CHANGED.trigger(other, other.getInventory(), water);
		helper.assertTrue(!QuestBookItem.readProgress(other).get(stackQuest).done(), "Water bottles are not brewed potions");
		ItemStack awkward = PotionContents.createItemStack(Items.POTION, Potions.AWKWARD);
		awkward.setCount(8);
		other.getInventory().add(awkward.copy());
		CriteriaTriggers.INVENTORY_CHANGED.trigger(other, other.getInventory(), awkward);
		helper.assertTrue(QuestBookItem.readProgress(other).get(stackQuest).done(), "Brewed base potions should count without effects");

		CriteriaTriggers.BREWED_POTION.trigger(player, new PotionContents(ModPotions.SUPREME_NIGHT_VISION));
		helper.assertTrue(!QuestBookItem.readProgress(player).get(supremeBrew).done(), "A long-base recipe should not count for the level II upgrade quest");
		CriteriaTriggers.CONSUME_ITEM.trigger(player, PotionContents.createItemStack(Items.POTION, ModPotions.SUPREME_NIGHT_VISION));
		helper.assertTrue(QuestBookItem.readProgress(player).get(drinkQuest).done(), "Any drinkable Supreme potion should count");
		CriteriaTriggers.BREWED_POTION.trigger(player, new PotionContents(ModPotions.SUPREME_SWIFTNESS));
		CriteriaTriggers.BREWED_POTION.trigger(player, new PotionContents(ModPotions.FATAL_SLOWNESS));
		var progress = QuestBookItem.readProgress(player);
		helper.assertTrue(progress.get(supremeBrew).done() && progress.get(fatalBrew).done(), "Strong-base Supreme and Fatal recipes should count");
		helper.succeed();
	}

	@GameTest
	public void fletchingQuestsCountTakenBatches(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		var eight = Wildways.id("quests/tip_eight_arrows");
		var sixtyFour = Wildways.id("quests/tip_stack_arrows");
		FletchingTableMenu menu = new FletchingTableMenu(1, player.getInventory());
		menu.getSlot(1).set(new ItemStack(Items.ARROW, 64));
		menu.getSlot(2).set(PotionContents.createItemStack(Items.LINGERING_POTION, Potions.POISON));
		helper.assertTrue(menu.getSlot(3).getItem().getCount() == 64, "The lingering potion should preview a full stack");
		helper.assertTrue(!QuestBookItem.readProgress(player).get(sixtyFour).done(), "A preview should not count");
		menu.quickMoveStack(player, 3);
		var progress = QuestBookItem.readProgress(player);
		helper.assertTrue(progress.get(sixtyFour).done() && !progress.get(eight).done(), "A stack should count separately from a batch of eight");

		menu.getSlot(1).set(new ItemStack(Items.ARROW, 8));
		menu.getSlot(2).set(PotionContents.createItemStack(Items.SPLASH_POTION, Potions.POISON));
		menu.quickMoveStack(player, 3);
		helper.assertTrue(QuestBookItem.readProgress(player).get(eight).done(), "Taking eight tipped arrows should count");
		helper.succeed();
	}

	@GameTest
	public void fatalPotionsCountWhenSplashOrCloudReachesAMob(GameTestHelper helper) {
		ServerPlayer thrower = helper.makeMockServerPlayerInLevel();
		var quest = Wildways.id("quests/use_fatal");
		var cow = helper.spawn(EntityTypes.COW, new BlockPos(1, 2, 1));
		ItemStack ordinary = PotionContents.createItemStack(Items.SPLASH_POTION, Potions.WEAKNESS);
		var splash = new ThrownSplashPotion(helper.getLevel(), thrower, ordinary);
		splash.setPos(cow.position());
		splash.onHitAsPotion(helper.getLevel(), ordinary, new EntityHitResult(cow));
		helper.assertTrue(!QuestBookItem.readProgress(thrower).get(quest).done(), "Ordinary splash potions should not count");

		ItemStack fatal = PotionContents.createItemStack(Items.SPLASH_POTION, ModPotions.FATAL_HARMING);
		splash = new ThrownSplashPotion(helper.getLevel(), thrower, fatal);
		var miss = cow.position().add(0, 20, 0);
		splash.setPos(miss);
		splash.onHitAsPotion(helper.getLevel(), fatal, BlockHitResult.miss(miss, Direction.UP, BlockPos.containing(miss)));
		helper.assertTrue(!QuestBookItem.readProgress(thrower).get(quest).done(), "A fatal splash without a mob should not count");

		splash.setPos(cow.position());
		splash.onHitAsPotion(helper.getLevel(), fatal, new EntityHitResult(cow));
		helper.assertTrue(QuestBookItem.readProgress(thrower).get(quest).done(), "Fatal splash should count for its thrower");

		ServerPlayer cloudOwner = helper.makeMockServerPlayerInLevel();
		var target = helper.spawn(EntityTypes.COW, new BlockPos(2, 2, 2));
		var cloud = new AreaEffectCloud(helper.getLevel(), target.getX(), target.getY(), target.getZ());
		cloud.setOwner(cloudOwner);
		cloud.setWaitTime(0);
		cloud.setPotionContents(new PotionContents(ModPotions.FATAL_SLOWNESS));
		helper.getLevel().addFreshEntity(cloud);
		helper.succeedWhen(() -> helper.assertTrue(QuestBookItem.readProgress(cloudOwner).get(quest).done(),
			"The fatal cloud should count for its owner once it reaches a mob"));
	}

	@GameTest
	public void packedNestRequiresContentsInInventory(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		var nest = Wildways.id("quests/nest");
		var packedNest = Wildways.id("quests/packed_nest");
		ItemStack empty = new ItemStack(ModBlocks.ENDERMITE_BOX);
		player.getInventory().add(empty.copy());
		CriteriaTriggers.INVENTORY_CHANGED.trigger(player, player.getInventory(), empty);
		helper.assertTrue(!QuestBookItem.readProgress(player).get(nest).done(), "Obtaining a nest is distinct from crafting it");
		helper.assertTrue(!QuestBookItem.readProgress(player).get(packedNest).done(), "An empty nest should not count");

		ItemStack packed = empty.copy();
		packed.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(new ItemStack(Items.STICK))));
		player.getInventory().add(packed.copy());
		CriteriaTriggers.INVENTORY_CHANGED.trigger(player, player.getInventory(), packed);
		helper.assertTrue(QuestBookItem.readProgress(player).get(packedNest).done(), "A nest holding an item should count");
		helper.succeed();
	}

	@GameTest
	public void collectorCountsUniqueEyesAcrossInventoryAndBoxes(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		var collection = Wildways.id("quests/collect_portal_eyes");
		var eyes = ModItems.PORTAL_EYES.stream()
			.filter(eye -> eye != ModItems.ENCHANTED_EYE && eye != ModItems.ATTUNED_EYE_OF_VOLCANITE)
			.toList();
		for (var eye : eyes.subList(0, 9)) {
			player.getInventory().add(new ItemStack(eye));
		}
		player.getInventory().add(new ItemStack(Items.ENDER_EYE));
		var enchantedEyeQuest = Wildways.id("quests/enchanted_eye");
		ItemStack blankEye = new ItemStack(ModItems.ENCHANTED_EYE);
		player.getInventory().add(blankEye.copy());
		CriteriaTriggers.INVENTORY_CHANGED.trigger(player, player.getInventory(), blankEye);
		helper.assertTrue(!QuestBookItem.readProgress(player).get(enchantedEyeQuest).done(), "The crafted eye still needs its enchantment");
		player.getInventory().add(new ItemStack(ModItems.EYE_OF_VOLCANITE));
		ItemStack nest = new ItemStack(ModBlocks.ENDERMITE_BOX);
		nest.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(
			new ItemStack(eyes.getFirst()), new ItemStack(eyes.get(9)), new ItemStack(ModItems.ATTUNED_EYE_OF_VOLCANITE)
		)));
		player.getInventory().add(nest);
		helper.assertTrue(QuestBookItem.readProgress(player).get(collection).equals(new QuestGuide.Progress(11, 12)),
			"Duplicates, vanilla eyes, and an unfinished enchanted eye should not increase the count");

		ItemStack shulker = new ItemStack(Items.SHULKER_BOX);
		shulker.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(new ItemStack(eyes.get(10)))));
		player.getInventory().add(shulker);
		helper.assertTrue(QuestBookItem.readProgress(player).get(collection).done(), "The twelfth unique eye in a shulker box should count");
		player.getInventory().clearContent();
		helper.assertTrue(QuestBookItem.readProgress(player).get(collection).done(), "The achievement should persist after using the eyes");
		var attunement = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
			.getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, Wildways.id("attunement")));
		blankEye.enchant(attunement, 1);
		player.getInventory().add(blankEye.copy());
		CriteriaTriggers.INVENTORY_CHANGED.trigger(player, player.getInventory(), blankEye);
		helper.assertTrue(QuestBookItem.readProgress(player).get(enchantedEyeQuest).done(), "An enchanted eye should count");
		helper.succeed();
	}
}
