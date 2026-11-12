package com.viclev.wildways;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/** Exercises real menu transactions, enchantment data and applied anvil mixins. */
public class EnchantingGameTests {
	private static final BlockPos TABLE = new BlockPos(3, 2, 3);

	private Holder.Reference<Enchantment> enchantment(GameTestHelper helper, ResourceKey<Enchantment> key) {
		return helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
	}

	private Player player(GameTestHelper helper, int levels) {
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.experienceLevel = levels;
		return player;
	}

	private ItemStack book(GameTestHelper helper, ResourceKey<Enchantment> key, int level) {
		ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
		book.enchant(this.enchantment(helper, key), level);
		return book;
	}

	private WildwaysEnchantingMenu table(GameTestHelper helper, Player player, int shelves) {
		helper.setBlock(TABLE, Blocks.ENCHANTING_TABLE);
		int placed = 0;
		for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
			if (placed++ < shelves) {
				helper.setBlock(TABLE.offset(offset), Blocks.BOOKSHELF);
			}
		}
		return new WildwaysEnchantingMenu(1, player.getInventory(), ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(TABLE)));
	}

	private void inputs(GameTestHelper helper, WildwaysEnchantingMenu menu, ResourceKey<Enchantment> key, int runeTier) {
		menu.getSlot(0).set(new ItemStack(Items.DIAMOND_PICKAXE));
		menu.getSlot(1).set(new ItemStack(Items.LAPIS_LAZULI, 8));
		menu.getSlot(2).set(this.book(helper, key, 5));
		menu.getSlot(3).set(new ItemStack(EnchantingItems.RUNES.get(runeTier - 1), 8));
	}

	@GameTest
	public void enchantingTableRetainsInputsWhenClosed(GameTestHelper helper) {
		Player player = this.player(helper, 10);
		WildwaysEnchantingMenu menu = this.table(helper, player, 0);
		this.inputs(helper, menu, Enchantments.UNBREAKING, 1);
		menu.removed(player);
		WorkstationInventory stored = (WorkstationInventory)helper.getLevel().getBlockEntity(helper.absolutePos(TABLE));
		helper.assertTrue(stored.getItem(0).is(Items.DIAMOND_PICKAXE) && stored.getItem(2).is(Items.ENCHANTED_BOOK),
			"Closing the table must leave the target and book inside the block");
		WildwaysEnchantingMenu reopened = new WildwaysEnchantingMenu(2, player.getInventory(),
			ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(TABLE)));
		helper.assertTrue(reopened.getSlot(0).hasItem() && reopened.getSlot(1).getItem().getCount() == 8
			&& reopened.getSlot(2).hasItem() && reopened.getSlot(3).getItem().getCount() == 8,
			"All enchanting inputs must return to their slots on reopening");
		helper.succeed();
	}

	@GameTest
	public void anvilRetainsInputsAndRenamesWithoutXp(GameTestHelper helper) {
		Player player = this.player(helper, 0);
		helper.setBlock(TABLE, Blocks.ANVIL);
		ContainerLevelAccess access = ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(TABLE));
		AnvilMenu menu = new AnvilMenu(1, player.getInventory(), access);
		menu.getSlot(0).set(new ItemStack(Items.NAME_TAG));
		menu.getSlot(1).set(new ItemStack(Items.IRON_INGOT));
		menu.removed(player);
		WorkstationInventory stored = (WorkstationInventory)helper.getLevel().getBlockEntity(helper.absolutePos(TABLE));
		helper.assertTrue(stored.getItem(0).is(Items.NAME_TAG) && stored.getItem(1).is(Items.IRON_INGOT),
			"Both anvil inputs must remain in the anvil after closing");
		AnvilMenu reopened = new AnvilMenu(2, player.getInventory(), access);
		helper.assertTrue(reopened.getSlot(0).hasItem() && reopened.getSlot(1).hasItem(),
			"Both anvil slots must be restored when reopened");
		reopened.getSlot(1).set(ItemStack.EMPTY);
		reopened.setItemName("Wildways");
		helper.assertTrue(reopened.getCost() == 0 && reopened.getSlot(2).hasItem() && reopened.getSlot(2).mayPickup(player),
			"Renaming a name tag alone must be free and takeable at level zero");
		reopened.clicked(2, 0, ContainerInput.PICKUP, player);
		helper.assertTrue(player.experienceLevel == 0 && reopened.getCarried().getHoverName().getString().equals("Wildways"),
			"Taking a renamed name tag must not use XP");
		helper.succeed();
	}

	@GameTest
	public void partialGapBlocksDoNotBlockBookshelves(GameTestHelper helper) {
		this.table(helper, this.player(helper, 0), 0);
		BlockPos offset = new BlockPos(2, 0, 0);
		helper.setBlock(TABLE.offset(offset), Blocks.BOOKSHELF);
		BlockPos gap = TABLE.offset(1, 0, 0);
		helper.setBlock(gap, Blocks.TORCH);
		BlockPos table = helper.absolutePos(TABLE);
		helper.assertTrue(EnchantingTableBlock.isValidBookShelf(helper.getLevel(), table, offset),
			"A torch between the table and shelf must not block enchanting power");
		helper.setBlock(gap, Blocks.CARPET.white());
		helper.assertTrue(EnchantingTableBlock.isValidBookShelf(helper.getLevel(), table, offset),
			"A carpet between the table and shelf must not block enchanting power");
		helper.setBlock(gap, Blocks.STONE);
		helper.assertFalse(EnchantingTableBlock.isValidBookShelf(helper.getLevel(), table, offset),
			"A full block must still block enchanting power");
		helper.succeed();
	}

	@GameTest
	public void consumesOneLapisAndRuneAndKeepsRecipeBook(GameTestHelper helper) {
		Player player = this.player(helper, 1);
		WildwaysEnchantingMenu menu = this.table(helper, player, 0);
		this.inputs(helper, menu, Enchantments.UNBREAKING, 1);
		helper.assertTrue(menu.getSlot(5).hasItem(), "Rune 1 must work with one level and no shelves");
		menu.clicked(5, 0, ContainerInput.PICKUP, player);
		helper.assertTrue(EnchantmentHelper.getItemEnchantmentLevel(this.enchantment(helper, Enchantments.UNBREAKING), menu.getCarried()) == 1,
			"The rune, not the original book level, must set the applied level");
		helper.assertTrue(player.experienceLevel == 0, "Exactly one level must be paid");
		helper.assertTrue(menu.getSlot(1).getItem().getCount() == 7 && menu.getSlot(3).getItem().getCount() == 7, "Exactly one lapis and rune must be consumed");
		helper.assertTrue(menu.getSlot(2).getItem().is(Items.ENCHANTED_BOOK), "Recipe book must remain");
		helper.assertFalse(menu.getSlot(0).hasItem() || menu.getSlot(5).hasItem(), "Input and result must not duplicate the tool");
		helper.succeed();
	}

	@GameTest
	public void universalCatalystRoundsUpAndBreaksAfterFourUses(GameTestHelper helper) {
		Player player = this.player(helper, 20);
		WildwaysEnchantingMenu menu = this.table(helper, player, 20);
		this.inputs(helper, menu, Enchantments.EFFICIENCY, 4);
		menu.getSlot(4).set(new ItemStack(EnchantingItems.CATALYST));
		helper.assertTrue(menu.getSlot(5).hasItem() && menu.experienceCost() == 2, "The universal catalyst must halve rune 4's cost");
		for (int tier : new int[]{1, 2, 3, 5}) {
			menu.getSlot(0).set(new ItemStack(Items.DIAMOND_PICKAXE));
			menu.getSlot(3).set(new ItemStack(EnchantingItems.RUNES.get(tier - 1), 8));
			helper.assertTrue(menu.getSlot(5).hasItem(), "Valid catalyst recipe must be ready");
			menu.clicked(5, 0, ContainerInput.PICKUP, player);
			menu.setCarried(ItemStack.EMPTY);
		}
		helper.assertTrue(player.experienceLevel == 13, "Runes 1, 2, 3 and 5 must cost 1, 1, 2 and 3 levels with the same catalyst");
		helper.assertFalse(menu.getSlot(4).hasItem(), "Catalyst must break on its fourth use");
		helper.succeed();
	}

	@GameTest
	public void questCreditsAppliedEnchantmentsAndActualCatalystSavings(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		GameType.SURVIVAL.updatePlayerAbilities(player.getAbilities());
		player.experienceLevel = 20;
		var enchant = Wildways.id("quests/rune_enchanting");
		var levelFive = Wildways.id("quests/enchant_level_five");
		var catalyst = Wildways.id("quests/use_enchanting_catalyst");
		WildwaysEnchantingMenu menu = this.table(helper, player, 20);

		this.inputs(helper, menu, Enchantments.UNBREAKING, 1);
		ItemStack alreadyEnchanted = new ItemStack(Items.DIAMOND_PICKAXE);
		alreadyEnchanted.enchant(this.enchantment(helper, Enchantments.EFFICIENCY), 5);
		menu.getSlot(0).set(alreadyEnchanted);
		menu.getSlot(4).set(new ItemStack(EnchantingItems.CATALYST));
		helper.assertTrue(menu.getSlot(5).hasItem(), "The level I enchantment should be available");
		helper.assertTrue(!QuestBookItem.readProgress(player).get(enchant).done(), "A preview should not count");
		menu.clicked(5, 0, ContainerInput.PICKUP, player);
		menu.setCarried(ItemStack.EMPTY);
		var progress = QuestBookItem.readProgress(player);
		helper.assertTrue(progress.get(enchant).done(), "Taking an enchanted item should count");
		helper.assertTrue(!progress.get(levelFive).done(), "An existing level V enchantment should not count");
		helper.assertTrue(!progress.get(catalyst).done(), "A catalyst cannot save XP on a one-level recipe");

		this.inputs(helper, menu, Enchantments.EFFICIENCY, 5);
		helper.assertTrue(menu.getSlot(5).hasItem() && menu.experienceCost() == 3, "Efficiency V should cost three levels with a catalyst");
		menu.quickMoveStack(player, 5);
		progress = QuestBookItem.readProgress(player);
		helper.assertTrue(progress.get(levelFive).done() && progress.get(catalyst).done(),
			"A new level V enchantment and its discount should count on extraction");
		Player expected = this.player(helper, 20);
		expected.giveExperienceLevels(-1);
		expected.giveExperiencePoints(25);
		expected.giveExperiencePoints(15);
		expected.giveExperienceLevels(-3);
		expected.giveExperiencePoints(25);
		expected.giveExperiencePoints(25);
		helper.assertTrue(player.experienceLevel == expected.experienceLevel
			&& Math.abs(player.experienceProgress - expected.experienceProgress) < 0.00001F,
			"Both enchantments should charge their levels before adding quest XP");
		helper.assertTrue(!progress.get(Wildways.id("quests/craft_rune")).done()
			&& !progress.get(Wildways.id("quests/craft_enchanting_catalyst")).done(),
			"Using a rune or catalyst is separate from crafting it");
		helper.succeed();
	}

	@GameTest
	public void questCreditsTakingAMaterialRepairAtTheAnvil(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		var mending = Wildways.id("quests/mending");
		var repair = Wildways.id("quests/repair_mending_item");
		ItemStack pickaxe = new ItemStack(Items.NETHERITE_PICKAXE);
		pickaxe.enchant(this.enchantment(helper, Enchantments.MENDING), 1);
		pickaxe.setDamageValue(20);
		player.getInventory().add(pickaxe.copy());
		CriteriaTriggers.INVENTORY_CHANGED.trigger(player, player.getInventory(), pickaxe);
		helper.assertTrue(QuestBookItem.readProgress(player).get(mending).done(), "Equipment with Mending should count");

		AnvilMenu menu = new AnvilMenu(1, player.getInventory());
		menu.getSlot(0).set(pickaxe.copy());
		menu.setItemName("Named pickaxe");
		menu.clicked(2, 0, ContainerInput.PICKUP, player);
		helper.assertTrue(menu.getCarried().isDamaged(), "A rename should not repair the pickaxe");
		helper.assertTrue(!QuestBookItem.readProgress(player).get(repair).done(), "Renaming should not count as a repair");
		menu.setCarried(ItemStack.EMPTY);

		menu.getSlot(0).set(pickaxe.copy());
		menu.getSlot(1).set(new ItemStack(Items.NETHERITE_SCRAP));
		helper.assertTrue(menu.getSlot(2).hasItem() && !menu.getSlot(2).getItem().isDamaged(), "The matching material should repair the pickaxe");
		helper.assertTrue(!QuestBookItem.readProgress(player).get(repair).done(), "A repair preview should not count");
		menu.quickMoveStack(player, 2);
		helper.assertTrue(QuestBookItem.readProgress(player).get(repair).done(), "Taking the material repair should count");
		helper.succeed();
	}

	@GameTest
	public void preciousEnchantmentsNeedGoldRuneAndLiveBookshelfChecks(GameTestHelper helper) {
		Player player = this.player(helper, 20);
		WildwaysEnchantingMenu menu = this.table(helper, player, 10);
		this.inputs(helper, menu, Enchantments.SILK_TOUCH, 2);
		helper.assertFalse(menu.getSlot(5).hasItem(), "Silk Touch must reject an iron rune");
		menu.getSlot(3).set(new ItemStack(EnchantingItems.RUNES.get(2)));
		helper.assertTrue(menu.getSlot(5).hasItem() && menu.experienceCost() == 3,
			"Silk Touch must accept a gold rune with ten shelves for three levels");
		for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
			helper.setBlock(TABLE.offset(offset), Blocks.AIR);
		}
		helper.assertFalse(menu.getSlot(5).mayPickup(player), "Removing shelves must invalidate an already visible result");
		menu.clicked(5, 0, ContainerInput.PICKUP, player);
		helper.assertTrue(menu.getCarried().isEmpty() && player.experienceLevel == 20, "Invalidated results must not charge or give items");
		helper.succeed();
	}

	@GameTest
	public void higherRunesKeepTheEnchantmentMaximumAndItsCost(GameTestHelper helper) {
		Player player = this.player(helper, 10);
		WildwaysEnchantingMenu menu = this.table(helper, player, 10);
		this.inputs(helper, menu, Enchantments.FORTUNE, 4);
		helper.assertTrue(menu.getSlot(5).hasItem() && menu.experienceCost() == 3,
			"A diamond rune must make Fortune III with only the gold-rune shelf and XP cost");
		helper.assertTrue(EnchantmentHelper.getItemEnchantmentLevel(this.enchantment(helper, Enchantments.FORTUNE),
			menu.getSlot(5).getItem()) == 3, "Higher runes must not exceed an enchantment's maximum level");
		menu.getSlot(3).set(new ItemStack(EnchantingItems.RUNES.get(4)));
		helper.assertTrue(menu.getSlot(5).hasItem() && menu.experienceCost() == 3,
			"A Volcanite rune must also work without increasing Fortune III's price");
		menu.getSlot(4).set(new ItemStack(EnchantingItems.CATALYST));
		helper.assertTrue(menu.getSlot(5).hasItem() && menu.experienceCost() == 2,
			"The catalyst must halve the effective cost, not the higher rune's tier");
		helper.succeed();
	}

	@GameTest
	public void allPreciousSingleLevelEnchantmentsUseGoldRune(GameTestHelper helper) {
		Player player = this.player(helper, 20);
		WildwaysEnchantingMenu menu = this.table(helper, player, 10);
		for (var entry : List.of(
			new Object[]{Enchantments.MENDING, Items.DIAMOND_PICKAXE},
			new Object[]{Enchantments.SILK_TOUCH, Items.DIAMOND_PICKAXE},
			new Object[]{Enchantments.CHANNELING, Items.TRIDENT},
			new Object[]{Enchantments.MULTISHOT, Items.CROSSBOW})) {
			@SuppressWarnings("unchecked")
			ResourceKey<Enchantment> enchantment = (ResourceKey<Enchantment>)entry[0];
			menu.getSlot(0).set(new ItemStack((Item)entry[1]));
			menu.getSlot(1).set(new ItemStack(Items.LAPIS_LAZULI));
			menu.getSlot(2).set(this.book(helper, enchantment, 1));
			menu.getSlot(3).set(new ItemStack(EnchantingItems.RUNES.get(1)));
			helper.assertFalse(menu.getSlot(5).hasItem(), "Precious level-one enchantments must reject iron runes");
			menu.getSlot(3).set(new ItemStack(EnchantingItems.RUNES.get(2)));
			helper.assertTrue(menu.getSlot(5).hasItem() && menu.experienceCost() == 3,
				"Each precious level-one enchantment must accept a gold rune for three levels");
			menu.getSlot(3).set(new ItemStack(EnchantingItems.RUNES.get(4)));
			helper.assertTrue(menu.getSlot(5).hasItem() && menu.experienceCost() == 3,
				"A more expensive rune must not raise a precious enchantment's cost");
		}
		helper.succeed();
	}

	@GameTest
	public void mendingRepairsNetheriteWithOneScrapForThreeLevels(GameTestHelper helper) {
		Player player = this.player(helper, 10);
		AnvilMenu menu = new AnvilMenu(1, player.getInventory());
		ItemStack pickaxe = new ItemStack(Items.NETHERITE_PICKAXE);
		pickaxe.setDamageValue(1000);
		menu.getSlot(0).set(pickaxe);
		menu.getSlot(1).set(new ItemStack(Items.NETHERITE_SCRAP, 2));
		helper.assertFalse(menu.getSlot(2).hasItem(), "Netherite Scrap must not bypass the Mending requirement");
		pickaxe.enchant(this.enchantment(helper, Enchantments.MENDING), 1);
		menu.createResult();
		helper.assertTrue(menu.getCost() == 3 && menu.getSlot(2).getItem().getDamageValue() == 0,
			"Mending alone must fully repair Netherite equipment with one Scrap for three levels");
		menu.clicked(2, 0, ContainerInput.PICKUP, player);
		helper.assertTrue(menu.getSlot(1).getItem().getCount() == 1 && player.experienceLevel == 7,
			"Only one Scrap and three levels must be consumed");
		helper.succeed();
	}

	@GameTest
	public void mendingFullyRepairsAtFixedCostAndDoesNotRepairFromXp(GameTestHelper helper) {
		Player player = this.player(helper, 20);
		AnvilMenu menu = new AnvilMenu(1, player.getInventory());
		ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
		tool.setDamageValue(1000);
		menu.getSlot(0).set(tool);
		menu.getSlot(1).set(new ItemStack(Items.DIAMOND, 4));
		helper.assertFalse(menu.getSlot(2).hasItem(), "Material repair without Mending must be rejected");
		tool.enchant(this.enchantment(helper, Enchantments.MENDING), 1);
		tool.enchant(this.enchantment(helper, Enchantments.EFFICIENCY), 5);
		tool.set(DataComponents.REPAIR_COST, 200);
		menu.createResult();
		helper.assertTrue(menu.getCost() == 5 && menu.getSlot(2).getItem().getDamageValue() == 0, "Mending must fully repair at the highest-enchantment cost");
		menu.clicked(2, 0, ContainerInput.PICKUP, player);
		helper.assertTrue(menu.getSlot(1).getItem().getCount() == 3 && player.experienceLevel == 15, "Repair must consume one material and five levels");
		helper.assertTrue(menu.getCarried().getOrDefault(DataComponents.REPAIR_COST, 0) == 200, "Material repair must not increase prior-work cost");
		helper.assertTrue(this.enchantment(helper, Enchantments.MENDING).value().getEffects(EnchantmentEffectComponents.REPAIR_WITH_XP).isEmpty(), "Mending must have no XP-orb repair effect");
		helper.succeed();
	}

	@GameTest
	public void attunementEyeUsesRecipeAndShiftClickConsumesOnce(GameTestHelper helper) {
		Player player = this.player(helper, 1);
		WildwaysEnchantingMenu menu = this.table(helper, player, 0);
		ResourceKey<Enchantment> attunement = ResourceKey.create(Registries.ENCHANTMENT, Wildways.id("attunement"));
		this.inputs(helper, menu, attunement, 1);
		menu.getSlot(0).set(new ItemStack(ModItems.ENCHANTED_EYE));
		helper.assertTrue(menu.getSlot(5).hasItem(), "Attunement book and rune 1 must prepare the eye");
		menu.clicked(5, 0, ContainerInput.QUICK_MOVE, player);
		helper.assertTrue(player.getInventory().contains(stack -> stack.is(ModItems.ENCHANTED_EYE) && stack.isEnchanted()), "Shift-click must deliver the enchanted eye");
		helper.assertTrue(player.experienceLevel == 0 && menu.getSlot(1).getItem().getCount() == 7
			&& menu.getSlot(3).getItem().getCount() == 7 && menu.getSlot(2).hasItem(), "Shift-click must charge once and keep the Attunement book");
		helper.assertFalse(menu.getSlot(0).hasItem() || menu.getSlot(5).hasItem(), "Shift-click must not duplicate the eye");
		helper.succeed();
	}

	@GameTest
	public void booksHaveNoLevelsButEquipmentRetainsThem(GameTestHelper helper) {
		ItemStack book = this.book(helper, Enchantments.EFFICIENCY, 5);
		helper.assertTrue(book.get(DataComponents.STORED_ENCHANTMENTS).getLevel(this.enchantment(helper, Enchantments.EFFICIENCY)) == 1, "Generated books must store a generic recipe");
		ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
		tool.enchant(this.enchantment(helper, Enchantments.EFFICIENCY), 5);
		helper.assertTrue(EnchantmentHelper.getItemEnchantmentLevel(this.enchantment(helper, Enchantments.EFFICIENCY), tool) == 5, "Equipment must retain its actual enchantment level");
		helper.succeed();
	}

	@GameTest
	public void newLibrariansGenerateExpectedOffersPerLevel(GameTestHelper helper) {
		Player player = this.player(helper, 0);
		for (int level = 1; level <= 5; level++) {
			Villager villager = helper.spawn(EntityTypes.VILLAGER, new BlockPos(2, 2, 2));
			villager.setVillagerData(villager.getVillagerData().withProfession(helper.getLevel().registryAccess(), VillagerProfession.LIBRARIAN).withLevel(level));
			var offers = villager.getOffers();
			int maximumOffers = level == 3 ? 3 : 2;
			helper.assertTrue(offers.size() <= maximumOffers, "A new librarian level must respect its offer limit");
			if (level == 1) {
				helper.assertTrue(offers.size() == 1 && offers.getFirst().getBaseCostA().is(Items.PAPER),
					"New novices must generate a paper purchase");
				offers.clear();
				offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 5), new ItemStack(Items.CHISELED_BOOKSHELF), 12, 1, 0.05F));
			}
			if (level == 3) {
				helper.assertTrue(offers.size() == 3 && offers.stream().anyMatch(offer -> offer.getBaseCostA().is(Items.INK_SAC)
					&& offer.getResult().is(Items.EMERALD)), "New journeymen must still buy ink sacs");
				helper.assertTrue(offers.removeIf(offer -> offer.getBaseCostA().is(Items.WRITABLE_BOOK)),
					"New journeymen must also buy writable books");
				helper.assertTrue(offers.removeIf(offer -> offer.getResult().is(Items.CHISELED_BOOKSHELF)),
					"New journeymen must generate a chiseled bookshelf sale");
			}
			UseEntityCallback.EVENT.invoker().interact(player, helper.getLevel(), InteractionHand.MAIN_HAND, villager, null);
			helper.assertFalse(offers.stream().anyMatch(offer -> offer.getResult().is(ModItems.BIOME_COMPASS)), "Librarians must not sell a biome compass");
			if (level == 1) {
				helper.assertTrue(offers.size() == 2 && offers.stream().anyMatch(offer -> offer.getResult().is(Items.BOOKSHELF)
					&& offer.getBaseCostA().getCount() == 9) && offers.stream().anyMatch(offer -> offer.getBaseCostA().is(Items.PAPER)
					&& offer.getBaseCostA().getCount() == 24 && offer.getResult().is(Items.EMERALD)),
					"An old novice must sell ordinary bookshelves and buy paper");
			}
			if (level < 3) {
				helper.assertFalse(offers.stream().anyMatch(offer -> offer.getResult().is(Items.CHISELED_BOOKSHELF)),
					"The chiseled bookshelf must not be available before journeyman");
			}
			if (level == 2) {
				helper.assertTrue(offers.stream().filter(offer -> offer.getResult().is(EnchantingItems.RUNES.get(0))
					|| offer.getResult().is(EnchantingItems.RUNES.get(1))).count() == 1, "Apprentices may sell exactly one of runes 1 and 2");
				helper.assertTrue(offers.stream().filter(offer -> offer.getResult().is(Items.EMERALD)
					&& offer.getBaseCostA().is(Items.BOOK)).count() == 1, "Apprentices also buy books");
			}
			if (level == 3) {
				helper.assertTrue(offers.stream().filter(offer -> offer.getBaseCostA().is(Items.WRITABLE_BOOK)
					&& offer.getBaseCostA().getCount() == 2 && offer.getResult().is(Items.EMERALD)).count() == 1,
					"The old ink-only journeyman must gain the writable book purchase at the original price");
				helper.assertTrue(offers.stream().filter(offer -> offer.getResult().is(Items.CHISELED_BOOKSHELF)
					&& offer.getBaseCostA().is(Items.EMERALD) && offer.getBaseCostA().getCount() == 5).count() == 1,
					"An old journeyman must gain a chiseled bookshelf sale for five emeralds");
			}
			if (level == 4) {
				helper.assertTrue(offers.stream().filter(offer -> offer.getResult().is(Items.CLOCK)
					|| offer.getResult().is(Items.COMPASS)).count() == 1, "Experts must sell either a clock or a compass");
				helper.assertTrue(offers.stream().filter(offer -> offer.getResult().is(EnchantingItems.RUNES.get(2))
					&& offer.getBaseCostA().is(Items.EMERALD) && offer.getBaseCostA().getCount() == 30).count() == 1,
					"Every expert must sell a Golden Rune for thirty emeralds");
			}
			if (level == 5) {
				var attunement = this.enchantment(helper, ResourceKey.create(Registries.ENCHANTMENT, Wildways.id("attunement")));
				helper.assertTrue(offers.stream().filter(offer -> offer.getResult().is(Items.ENCHANTED_BOOK)).count() == 2,
					"Masters must offer the regional book and Attunement");
				helper.assertTrue(offers.stream().filter(offer -> offer.getResult().is(Items.ENCHANTED_BOOK)
					&& offer.getResult().getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).getLevel(attunement) == 1
					&& offer.getBaseCostA().getCount() == 18).count() == 1, "Every master must sell Attunement for eighteen emeralds");
			}
		}
		helper.succeed();
	}

	@GameTest
	public void existingLibrariansLoseExcessTradesWithoutDonations(GameTestHelper helper) {
		Player player = this.player(helper, 0);
		Villager villager = helper.spawn(EntityTypes.VILLAGER, new BlockPos(2, 2, 2));
		var registries = helper.getLevel().registryAccess();
		villager.setVillagerData(villager.getVillagerData().withProfession(registries, VillagerProfession.LIBRARIAN)
			.withType(registries, VillagerType.PLAINS).withLevel(5));
		var offers = villager.getOffers();
		offers.removeIf(offer -> offer.getResult().is(Items.ENCHANTED_BOOK));
		var attunement = this.enchantment(helper, ResourceKey.create(Registries.ENCHANTMENT, Wildways.id("attunement")));
		offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 9), new ItemStack(Items.BOOKSHELF), 12, 1, 0.05F));
		offers.add(new MerchantOffer(new ItemCost(Items.PAPER, 24), new ItemStack(Items.EMERALD), 16, 2, 0.05F));
		MerchantOffer chiseledBookshelfOffer = new MerchantOffer(new ItemCost(Items.EMERALD, 5), new ItemStack(Items.CHISELED_BOOKSHELF), 12, 1, 0.05F);
		chiseledBookshelfOffer.increaseUses();
		offers.add(chiseledBookshelfOffer);
		offers.add(new MerchantOffer(new ItemCost(Items.BOOK, 4), new ItemStack(Items.EMERALD), 12, 10, 0.05F));
		offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 10), new ItemStack(EnchantingItems.RUNES.get(0)), 12, 5, 0.05F));
		offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 20), new ItemStack(EnchantingItems.RUNES.get(1)), 12, 5, 0.05F));
		MerchantOffer writableBookOffer = new MerchantOffer(new ItemCost(Items.WRITABLE_BOOK, 2), new ItemStack(Items.EMERALD), 12, 30, 0.05F);
		writableBookOffer.increaseUses();
		offers.add(writableBookOffer);
		MerchantOffer clockOffer = new MerchantOffer(new ItemCost(Items.EMERALD, 5), new ItemStack(Items.CLOCK), 12, 15, 0.05F);
		clockOffer.increaseUses();
		offers.add(clockOffer);
		offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 5), new ItemStack(Items.COMPASS), 12, 15, 0.05F));
		offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 5), new ItemStack(ModItems.BIOME_COMPASS), 12, 10, 0.05F));
		offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 3), new ItemStack(Items.DYED_CANDLE.red()), 12, 30, 0.05F));
		offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 18), new ItemStack(EnchantingItems.CATALYST), 12, 30, 0.05F));
		offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 18), this.book(helper, Enchantments.MENDING, 1), 12, 30, 0.05F));
		player.setItemInHand(InteractionHand.MAIN_HAND, this.book(helper, Enchantments.UNBREAKING, 1));
		UseEntityCallback.EVENT.invoker().interact(player, helper.getLevel(), InteractionHand.MAIN_HAND, villager, null);
		helper.assertTrue(offers.stream().filter(offer -> offer.getResult().is(Items.BOOKSHELF)).count() == 1,
			"Existing ordinary bookshelf sales must remain available without duplication");
		helper.assertTrue(offers.stream().filter(offer -> offer.getBaseCostA().is(Items.PAPER)).count() == 1,
			"The novice paper purchase must remain available");
		helper.assertTrue(offers.stream().filter(offer -> offer.getResult().is(Items.CHISELED_BOOKSHELF)).count() == 1
			&& offers.contains(chiseledBookshelfOffer) && chiseledBookshelfOffer.getUses() == 1,
			"Existing masters must keep their chiseled bookshelf sale and its uses");
		helper.assertTrue(offers.stream().filter(offer -> offer.getResult().is(EnchantingItems.RUNES.get(0))
			|| offer.getResult().is(EnchantingItems.RUNES.get(1))).count() == 1, "Only one apprentice rune may remain");
		helper.assertTrue(offers.stream().filter(offer -> offer.getResult().is(Items.CLOCK) || offer.getResult().is(Items.COMPASS)).count() == 1
			&& offers.contains(clockOffer) && clockOffer.getUses() == 1, "Migration must preserve the used navigation offer");
		helper.assertTrue(offers.stream().filter(offer -> offer.getBaseCostA().is(Items.WRITABLE_BOOK)).count() == 1
			&& offers.contains(writableBookOffer) && writableBookOffer.getUses() == 1, "Migration must retain a used writable book purchase");
		helper.assertFalse(offers.stream().anyMatch(offer -> offer.getResult().is(Items.DYED_CANDLE.red())
			|| offer.getResult().is(Items.DYED_CANDLE.yellow())), "The old master candle offer must be removed");
		helper.assertFalse(offers.stream().anyMatch(offer -> offer.getResult().is(ModItems.BIOME_COMPASS)),
			"Old librarian biome compass offers must be removed");
		helper.assertFalse(offers.stream().anyMatch(offer -> offer.getResult().is(EnchantingItems.CATALYST)),
			"Old catalyst offers must be removed");
		helper.assertTrue(offers.stream().filter(offer -> offer.getResult().is(Items.ENCHANTED_BOOK)).count() == 2
			&& offers.stream().anyMatch(offer -> ItemStack.matches(offer.getResult(), this.book(helper, Enchantments.EFFICIENCY, 1))),
			"The old master must gain the regional book and lose the unrelated book");
		helper.assertTrue(offers.stream().anyMatch(offer -> offer.getResult().is(Items.ENCHANTED_BOOK)
			&& offer.getResult().getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).getLevel(attunement) == 1
			&& offer.getBaseCostA().getCount() == 18), "Master librarians must sell Attunement for eighteen emeralds");
		helper.assertTrue(player.getMainHandItem().is(Items.ENCHANTED_BOOK), "Interacting must not donate the held book");
		UseEntityCallback.EVENT.invoker().interact(player, helper.getLevel(), InteractionHand.MAIN_HAND, villager, null);
		helper.assertTrue(offers.stream().filter(offer -> offer.getResult().is(EnchantingItems.RUNES.get(2))).count() == 1,
			"Opening an existing master twice must grant exactly one Golden Rune offer");
		helper.assertTrue(offers.stream().filter(offer -> offer.getResult().is(Items.ENCHANTED_BOOK)).count() == 2,
			"Opening the same librarian twice must not duplicate either master book");
		helper.succeed();
	}

	@GameTest
	public void regionalBookTradesFollowAllSevenVillagerTypes(GameTestHelper helper) {
		var level = helper.getLevel();
		Player player = this.player(helper, 0);
		var regionalBooks = Map.of(VillagerType.SNOW, Enchantments.UNBREAKING,
			VillagerType.SAVANNA, Enchantments.SHARPNESS, VillagerType.JUNGLE, Enchantments.SHARPNESS,
			VillagerType.PLAINS, Enchantments.EFFICIENCY, VillagerType.TAIGA, Enchantments.EFFICIENCY,
			VillagerType.SWAMP, Enchantments.PROTECTION, VillagerType.DESERT, Enchantments.PROTECTION);
		for (var regionalBook : regionalBooks.entrySet()) {
			Villager villager = helper.spawn(EntityTypes.VILLAGER, new BlockPos(2, 2, 2));
			villager.setVillagerData(villager.getVillagerData().withProfession(level.registryAccess(), VillagerProfession.LIBRARIAN)
				.withType(level.registryAccess(), regionalBook.getKey()).withLevel(5));
			var offers = villager.getOffers();
			helper.assertTrue(offers.size() == 1, "Each master type must generate one regional offer");
			MerchantOffer offer = offers.getFirst();
			int price = offer.getBaseCostA().getCount();
			helper.assertTrue(ItemStack.matches(offer.getResult(), this.book(helper, regionalBook.getValue(), 1))
				&& offer.getBaseCostA().is(Items.EMERALD) && price >= 10 && price <= 38 && price % 2 == 0
				&& offer.getCostB().is(Items.BOOK) && offer.getCostB().getCount() == 1 && offer.getPriceMultiplier() == 0.2F,
				"Wrong regional book or price for " + regionalBook.getKey().identifier());
			offer.increaseUses();
			UseEntityCallback.EVENT.invoker().interact(player, level, InteractionHand.MAIN_HAND, villager, null);
			helper.assertTrue(offers.contains(offer) && offer.getUses() == 1 && offer.getBaseCostA().getCount() == price,
				"Opening the trade must retain the generated price and its uses");

			int offerIndex = offers.indexOf(offer);
			MerchantOffer legacyOffer = new MerchantOffer(new ItemCost(Items.EMERALD, 20), offer.getItemCostB(),
				offer.getResult(), 12, 12, 30, 0.05F, 2);
			offers.set(offerIndex, legacyOffer);
			UseEntityCallback.EVENT.invoker().interact(player, level, InteractionHand.MAIN_HAND, villager, null);
			MerchantOffer migrated = offers.get(offerIndex);
			int migratedPrice = migrated.getBaseCostA().getCount();
			helper.assertTrue(migrated != legacyOffer && migrated.getPriceMultiplier() == 0.2F
				&& migratedPrice >= 10 && migratedPrice <= 38 && migratedPrice % 2 == 0
				&& migrated.isOutOfStock() && migrated.getDemand() == 2, "Old offers must migrate without resetting stock or demand");
			UseEntityCallback.EVENT.invoker().interact(player, level, InteractionHand.MAIN_HAND, villager, null);
			helper.assertTrue(offers.get(offerIndex) == migrated, "The migrated price must remain fixed after reopening the trade");
		}
		helper.succeed();
	}

	@GameTest
	public void runeFiveRequiresRefinedVolcaniteAndCatalystRecipeProducesFourUses(GameTestHelper helper) {
		for (Item side : List.of(Items.SHULKER_SHELL, Items.ECHO_SHARD)) {
			CraftingInput input = this.craftingInput(Items.DIAMOND, side, ModItems.VOLCANITE);
			var recipe = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
			helper.assertTrue(recipe.isPresent(), "Both rune 5 recipes must load and match refined Volcanite");
			ItemStack result = recipe.orElseThrow().value().assemble(input);
			helper.assertTrue(result.is(EnchantingItems.RUNES.get(4)) && result.getCount() == 1, "Both variants must produce exactly one rune 5");
			for (Item invalidCenter : List.of(Items.CUT_COPPER_STAIRS.weathering().oxidized(), ModItems.RAW_VOLCANITE)) {
				helper.assertTrue(helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING,
					this.craftingInput(Items.DIAMOND, side, invalidCenter), helper.getLevel()).isEmpty(), "Copper stairs and raw material must not substitute for refined Volcanite");
			}
		}
		CraftingInput input = this.craftingInput(Items.LAPIS_BLOCK, Items.BLAZE_POWDER, Items.SCULK_CATALYST);
		var recipe = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
		helper.assertTrue(recipe.isPresent(), "The catalyst recipe from the supplied image must load");
		ItemStack result = recipe.orElseThrow().value().assemble(input);
		helper.assertTrue(result.is(EnchantingItems.CATALYST) && result.getCount() == 1 && result.getDamageValue() == 0
			&& result.getMaxDamage() == 4, "Crafting must produce one undamaged universal catalyst with four uses");
		helper.succeed();
	}

	@GameTest
	public void ironAndDiamondRunesUseUpdatedIngredients(GameTestHelper helper) {
		for (var entry : List.of(
			new Object[]{Items.REDSTONE, Items.COPPER_INGOT, Items.IRON_INGOT, EnchantingItems.RUNES.get(1)},
			new Object[]{Items.EMERALD, Items.AMETHYST_SHARD, Items.DIAMOND, EnchantingItems.RUNES.get(3)})) {
			Item top = (Item)entry[0];
			Item side = (Item)entry[1];
			Item center = (Item)entry[2];
			Item result = (Item)entry[3];
			CraftingInput input = this.craftingInput(top, side, center);
			var recipe = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
			helper.assertTrue(recipe.isPresent() && recipe.orElseThrow().value().assemble(input).is(result),
				"The updated iron and diamond rune designs must craft their respective runes");
		}
		helper.assertTrue(helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING,
			this.craftingInput(Items.AMETHYST_SHARD, Items.AMETHYST_SHARD, Items.IRON_INGOT), helper.getLevel()).isEmpty(),
			"The old iron-rune recipe must no longer work");
		helper.assertTrue(helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING,
			this.craftingInput(Items.EMERALD, Items.EMERALD, Items.DIAMOND), helper.getLevel()).isEmpty(),
			"The old diamond-rune recipe must no longer work");
		helper.succeed();
	}

	private CraftingInput craftingInput(Item top, Item side, Item center) {
		Item brick = Items.CHISELED_STONE_BRICKS;
		return CraftingInput.of(3, 3, List.of(brick, top, brick, side, center, side, brick, top, brick).stream().map(ItemStack::new).toList());
	}

	@GameTest
	public void plannedMasterTradesKeepSmithEquipmentAndWandererAddsBookAndExplorationOffer(GameTestHelper helper) {
		Player player = this.player(helper, 0);
		for (var entry : List.of(
			new Object[]{VillagerProfession.TOOLSMITH, Enchantments.FORTUNE},
			new Object[]{VillagerProfession.MASON, Enchantments.SILK_TOUCH},
			new Object[]{VillagerProfession.WEAPONSMITH, Enchantments.LOOTING},
			new Object[]{VillagerProfession.FLETCHER, Enchantments.INFINITY})) {
			@SuppressWarnings("unchecked")
			ResourceKey<Enchantment> target = (ResourceKey<Enchantment>)entry[1];
			var profession = (net.minecraft.resources.ResourceKey<VillagerProfession>)entry[0];
			Villager villager = helper.spawn(EntityTypes.VILLAGER, new BlockPos(2, 2, 2));
			villager.setVillagerData(villager.getVillagerData()
				.withProfession(helper.getLevel().registryAccess(), profession).withLevel(5));
			int before = villager.getOffers().size();
			Item equipment = profession.equals(VillagerProfession.TOOLSMITH) ? ProgressionItems.AMETHYST_PICKAXE
				: profession.equals(VillagerProfession.WEAPONSMITH) ? ProgressionItems.AMETHYST_SWORD : null;
			UseEntityCallback.EVENT.invoker().interact(player, helper.getLevel(), InteractionHand.MAIN_HAND, villager, null);
			helper.assertTrue(villager.getOffers().size() == before + (equipment == null ? 0 : 1), "The master book must keep smith equipment available");
			helper.assertTrue(villager.getOffers().stream().anyMatch(offer ->
				offer.getResult().getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)
					.getLevel(this.enchantment(helper, target)) == 1), "The guaranteed master book must be present");
			if (equipment != null) {
				MerchantOffer equipmentOffer = villager.getOffers().stream().filter(offer -> offer.getResult().is(equipment)).findFirst().orElseThrow();
				helper.assertFalse(equipmentOffer.getResult().getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty(),
					"A master smith must still sell enchanted equipment");
				// Simulate an existing master whose equipment was replaced by the guaranteed book.
				villager.getOffers().remove(equipmentOffer);
				UseEntityCallback.EVENT.invoker().interact(player, helper.getLevel(), InteractionHand.MAIN_HAND, villager, null);
				UseEntityCallback.EVENT.invoker().interact(player, helper.getLevel(), InteractionHand.MAIN_HAND, villager, null);
				helper.assertTrue(villager.getOffers().stream().filter(offer -> offer.getResult().is(equipment)).count() == 1,
					"An existing master must regain the equipment offer exactly once");
			}
		}
		WanderingTrader trader = helper.spawn(EntityTypes.WANDERING_TRADER, new BlockPos(3, 2, 3));
		int before = trader.getOffers().size();
		UseEntityCallback.EVENT.invoker().interact(player, helper.getLevel(), InteractionHand.MAIN_HAND, trader, null);
		helper.assertTrue(trader.getOffers().size() == before + 2, "The wanderer must add a book and one exploration offer");
		helper.assertTrue(trader.getOffers().get(before).getResult().is(Items.ENCHANTED_BOOK), "The book offer must remain available");
		MerchantOffer exploration = trader.getOffers().get(before + 1);
		int price = exploration.getBaseCostA().getCount();
		boolean validResult = exploration.getResult().is(ModItems.BIOME_COMPASS) ? price == 20
			: StructureMaps.isTradeMap(exploration.getResult()) && exploration.getResult().has(DataComponents.MAP_ID) && price >= 12 && price <= 20;
		helper.assertTrue(validResult && exploration.getBaseCostA().is(Items.EMERALD)
			&& exploration.getMaxUses() == 1 && !exploration.isOutOfStock(), "A new trader must have one valid exploration offer");
		exploration.increaseUses();
		UseEntityCallback.EVENT.invoker().interact(player, helper.getLevel(), InteractionHand.MAIN_HAND, trader, null);
		helper.assertTrue(trader.getOffers().size() == before + 2 && trader.getOffers().get(before + 1).isOutOfStock(),
			"Reopening the menu after a purchase must keep the exploration offer sold out");
		for (boolean bought : List.of(false, true)) {
			MerchantOffer legacyOffer = new MerchantOffer(new ItemCost(Items.EMERALD, 20), new ItemStack(ModItems.BIOME_COMPASS), 12, 10, 0.0F);
			if (bought) {
				legacyOffer.increaseUses();
			}
			trader.getOffers().set(before + 1, legacyOffer);
			UseEntityCallback.EVENT.invoker().interact(player, helper.getLevel(), InteractionHand.MAIN_HAND, trader, null);
			MerchantOffer migrated = trader.getOffers().get(before + 1);
			helper.assertTrue(migrated.getMaxUses() == 1 && migrated.isOutOfStock() == bought,
				"An old trader must keep the compass in stock only if it has never been sold");
		}
		helper.succeed();
	}

	@GameTest
	public void masterArmorersRollTwoOfThreeOffers(GameTestHelper helper) {
		var level = helper.getLevel();
		Villager villager = helper.spawn(EntityTypes.VILLAGER, new BlockPos(2, 2, 2));
		villager.setVillagerData(villager.getVillagerData().withProfession(level.registryAccess(), VillagerProfession.ARMORER).withLevel(5));
		var tradeSet = level.registryAccess().lookupOrThrow(Registries.TRADE_SET)
			.getOrThrow(villager.getVillagerData().profession().value().getTrades(5)).value();
		LootParams params = new LootParams.Builder(level)
			.withParameter(LootContextParams.ORIGIN, villager.position())
			.withParameter(LootContextParams.THIS_ENTITY, villager)
			.withParameter(LootContextParams.ADDITIONAL_COST_COMPONENT_ALLOWED, Unit.INSTANCE)
			.create(LootContextParamSets.VILLAGER_TRADE);
		LootContext context = new LootContext.Builder(params).withOptionalRandomSeed(42L).create(Optional.empty());
		helper.assertTrue(tradeSet.trades().size() == 3, "The master armorer must have three candidates, found " + tradeSet.trades().size());
		helper.assertTrue(tradeSet.calculateNumberOfTrades(context) == 2 && !tradeSet.allowDuplicates(),
			"The master armorer must draw two distinct offers");
		var candidates = tradeSet.trades().stream().map(trade -> trade.value().getOffer(context)).toList();
		helper.assertTrue(candidates.stream().allMatch(offer -> offer != null), "All master armorer candidates must generate a valid offer");
		var expectedItems = Set.of(ProgressionItems.AMETHYST_HELMET, ProgressionItems.AMETHYST_CHESTPLATE, Items.ENCHANTED_BOOK);
		helper.assertTrue(candidates.stream().map(offer -> offer.getResult().getItem()).collect(Collectors.toSet()).equals(expectedItems),
			"The master pool must contain an amethyst helmet, an amethyst chestplate and an enchanted book");
		var thornsOffer = candidates.stream().filter(offer -> offer.getResult().is(Items.ENCHANTED_BOOK)).findFirst().orElseThrow();
		helper.assertTrue(ItemStack.matches(thornsOffer.getResult(), this.book(helper, Enchantments.THORNS, 1))
			&& thornsOffer.getBaseCostA().is(Items.EMERALD) && thornsOffer.getBaseCostA().getCount() == 20 && thornsOffer.getCostB().isEmpty(),
			"The enchanted book must be Thorns for twenty emeralds");

		var offers = villager.getOffers();
		helper.assertTrue(offers.size() == 2 && offers.get(0).getResult().getItem() != offers.get(1).getResult().getItem()
			&& offers.stream().allMatch(offer -> expectedItems.contains(offer.getResult().getItem())),
			"A fresh master armorer must generate exactly two distinct offers from the master pool");
		var originalOffers = List.copyOf(offers);
		UseEntityCallback.EVENT.invoker().interact(this.player(helper, 0), level, InteractionHand.MAIN_HAND, villager, null);
		helper.assertTrue(offers.equals(originalOffers), "Opening the trades must preserve the two rolled offers");
		helper.succeed();
	}

	@GameTest
	public void savedDiamondEquipmentTradesKeepEnchantmentsAndStock(GameTestHelper helper) {
		var level = helper.getLevel();
		Villager villager = helper.spawn(EntityTypes.VILLAGER, new BlockPos(2, 2, 2));
		villager.setVillagerData(villager.getVillagerData().withProfession(level.registryAccess(), VillagerProfession.ARMORER).withLevel(4));
		var protection = this.enchantment(helper, Enchantments.PROTECTION);
		ItemStack boots = new ItemStack(Items.DIAMOND_BOOTS);
		boots.enchant(protection, 3);
		var offers = villager.getOffers();
		LootParams params = new LootParams.Builder(level)
			.withParameter(LootContextParams.ORIGIN, villager.position())
			.withParameter(LootContextParams.THIS_ENTITY, villager)
			.withParameter(LootContextParams.ADDITIONAL_COST_COMPONENT_ALLOWED, Unit.INSTANCE)
			.create(LootContextParamSets.VILLAGER_TRADE);
		var bootsTradeKey = ResourceKey.create(Registries.VILLAGER_TRADE,
			Identifier.withDefaultNamespace("armorer/4/emerald_enchanted_diamond_boots"));
		var bootsTrade = level.registryAccess().lookupOrThrow(Registries.VILLAGER_TRADE).getOrThrow(bootsTradeKey).value();
		LootContext context = new LootContext.Builder(params).withOptionalRandomSeed(42L).create(Optional.empty());
		MerchantOffer generated = bootsTrade.getOffer(context);
		helper.assertTrue(generated != null && generated.getResult().is(ProgressionItems.AMETHYST_BOOTS)
			&& !generated.getResult().getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty(),
			"The standard boots trade must generate enchanted amethyst boots");
		int originalPrice = generated.getItemCostA().count();
		int price = Math.round(originalPrice * 0.75F);
		helper.assertTrue(generated.getBaseCostA().getCount() == price && generated.getCostA().getCount() == price,
			"The display and purchase must use 75 percent of the full enchanted price");
		ItemStack emeralds = new ItemStack(Items.EMERALD, price);
		helper.assertTrue(generated.take(emeralds, ItemStack.EMPTY) && emeralds.isEmpty(), "The discounted emerald price must complete the purchase");
		MerchantOffer savedOffer = new MerchantOffer(new ItemCost(Items.EMERALD, 18), Optional.of(new ItemCost(Items.DIAMOND, 2)),
			boots, 3, 3, 15, 0.2F, 2);
		savedOffer.setSpecialPriceDiff(-1);
		offers.add(savedOffer);
		Player player = this.player(helper, 0);
		UseEntityCallback.EVENT.invoker().interact(player, level, InteractionHand.MAIN_HAND, villager, null);
		MerchantOffer migrated = offers.getLast();
		helper.assertTrue(migrated.getResult().is(ProgressionItems.AMETHYST_BOOTS)
			&& migrated.getResult().getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).getLevel(protection) == 3,
			"A saved diamond offer must keep its rolled enchantment");
		helper.assertTrue(migrated.isOutOfStock() && migrated.getDemand() == 2 && migrated.getXp() == 15
			&& migrated.getItemCostA().count() == 18 && migrated.getBaseCostA().getCount() == 14 && migrated.getCostA().getCount() == 18
			&& ItemStack.matches(migrated.getCostB(), savedOffer.getCostB()),
			"Migration must preserve stock and the second cost, and apply demand and reputation to the rounded base price");
		UseEntityCallback.EVENT.invoker().interact(player, level, InteractionHand.MAIN_HAND, villager, null);
		helper.assertTrue(offers.getLast() == migrated, "Reopening a converted offer must retain it");
		helper.succeed();
	}

	@GameTest
	public void mendingBookRecipeRequiresAProtectionBook(GameTestHelper helper) {
		ItemStack precursor = this.book(helper, Enchantments.PROTECTION, 1);
		CraftingInput input = CraftingInput.of(3, 3, List.of(
			new ItemStack(Items.LAPIS_LAZULI), new ItemStack(Items.GHAST_TEAR), new ItemStack(Items.LAPIS_LAZULI),
			new ItemStack(ModItems.VOLCANITE), precursor, new ItemStack(ModItems.VOLCANITE),
			new ItemStack(Items.LAPIS_LAZULI), new ItemStack(Items.EXPERIENCE_BOTTLE), new ItemStack(Items.LAPIS_LAZULI)));
		var recipe = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
		helper.assertTrue(recipe.isPresent(), "The planned Mending recipe must match the exact ingredients");
		ItemStack result = recipe.orElseThrow().value().assemble(input);
		helper.assertTrue(result.is(Items.ENCHANTED_BOOK)
			&& result.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)
				.getLevel(this.enchantment(helper, Enchantments.MENDING)) == 1,
			"The recipe must produce a level-one Mending book");
		CraftingInput wrongBook = CraftingInput.of(3, 3, List.of(
			new ItemStack(Items.LAPIS_LAZULI), new ItemStack(Items.GHAST_TEAR), new ItemStack(Items.LAPIS_LAZULI),
			new ItemStack(ModItems.VOLCANITE), this.book(helper, Enchantments.UNBREAKING, 1), new ItemStack(ModItems.VOLCANITE),
			new ItemStack(Items.LAPIS_LAZULI), new ItemStack(Items.EXPERIENCE_BOTTLE), new ItemStack(Items.LAPIS_LAZULI)));
			helper.assertTrue(helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, wrongBook, helper.getLevel()).isEmpty(),
			"An unrelated enchanted book must not substitute for Protection");
		helper.succeed();
	}

	@GameTest
	public void lightningTransformsDroppedBookIntoChanneling(GameTestHelper helper) {
		ItemEntity dropped = new ItemEntity(helper.getLevel(), 3.5, 2.0, 3.5, new ItemStack(Items.BOOK, 2));
		helper.getLevel().addFreshEntity(dropped);
		dropped.thunderHit(helper.getLevel(), null);
		ItemStack result = dropped.getItem();
		helper.assertTrue(result.is(Items.ENCHANTED_BOOK), "A lightning-struck ordinary book must become enchanted");
		helper.assertTrue(result.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)
			.getLevel(this.enchantment(helper, Enchantments.CHANNELING)) == 1,
			"A lightning-struck book must carry Channeling");
		helper.assertTrue(result.getCount() == 1, "Only one book from a stack should transform per lightning strike");
		helper.succeed();
	}

	@GameTest
	public void anvilRejectsBooksButCombinesEquipmentAboveFortyLevels(GameTestHelper helper) {
		Player player = this.player(helper, 200);
		AnvilMenu menu = new AnvilMenu(1, player.getInventory());
		ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
		tool.setDamageValue(1000);
		tool.set(DataComponents.REPAIR_COST, 25);
		menu.getSlot(0).set(tool);
		menu.getSlot(1).set(this.book(helper, Enchantments.EFFICIENCY, 5));
		helper.assertFalse(menu.getSlot(2).hasItem(), "Recipe books must not apply in the anvil");
		ItemStack sacrifice = new ItemStack(Items.DIAMOND_PICKAXE);
		sacrifice.setDamageValue(1000);
		sacrifice.set(DataComponents.REPAIR_COST, 25);
		sacrifice.enchant(this.enchantment(helper, Enchantments.UNBREAKING), 3);
		menu.getSlot(1).set(sacrifice);
		helper.assertTrue(menu.getCost() > 40 && menu.getSlot(2).hasItem(), "Equipment combination above forty levels must remain available");
		int combinationCost = menu.getCost();
		menu.setItemName("Reforged");
		helper.assertTrue(menu.getCost() == combinationCost,
			"Adding a name during a combination must not add an XP level");
		helper.assertTrue(menu.getSlot(2).getItem().getOrDefault(DataComponents.REPAIR_COST, 0) == 26, "Prior-work cost must increase by one instead of doubling");
		helper.succeed();
	}
}
