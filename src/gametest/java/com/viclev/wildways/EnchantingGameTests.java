package com.viclev.wildways;

import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
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
	public void preciousEnchantmentsNeedTierFiveAndLiveBookshelfChecks(GameTestHelper helper) {
		Player player = this.player(helper, 20);
		WildwaysEnchantingMenu menu = this.table(helper, player, 20);
		this.inputs(helper, menu, Enchantments.SILK_TOUCH, 1);
		helper.assertFalse(menu.getSlot(5).hasItem(), "Silk Touch must reject a tier 1 rune");
		menu.getSlot(3).set(new ItemStack(EnchantingItems.RUNES.get(4)));
		helper.assertTrue(menu.getSlot(5).hasItem(), "Silk Touch must accept tier 5 with twenty shelves");
		for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
			helper.setBlock(TABLE.offset(offset), Blocks.AIR);
		}
		helper.assertFalse(menu.getSlot(5).mayPickup(player), "Removing shelves must invalidate an already visible result");
		menu.clicked(5, 0, ContainerInput.PICKUP, player);
		helper.assertTrue(menu.getCarried().isEmpty() && player.experienceLevel == 20, "Invalidated results must not charge or give items");
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
	public void newLibrariansHaveAtMostTwoOffersPerLevel(GameTestHelper helper) {
		Player player = this.player(helper, 0);
		for (int level = 1; level <= 5; level++) {
			Villager villager = helper.spawn(EntityTypes.VILLAGER, new BlockPos(2, 2, 2));
			villager.setVillagerData(villager.getVillagerData().withProfession(helper.getLevel().registryAccess(), VillagerProfession.LIBRARIAN).withLevel(level));
			var offers = villager.getOffers();
			helper.assertTrue(offers.size() <= 2, "A new librarian level must generate at most two offers");
			UseEntityCallback.EVENT.invoker().interact(player, helper.getLevel(), InteractionHand.MAIN_HAND, villager, null);
			if (level == 1) {
				helper.assertTrue(offers.size() == 2 && offers.stream().anyMatch(offer -> offer.getResult().is(Items.BOOKSHELF)
					&& offer.getBaseCostA().getCount() == 9), "Novices need one ordinary Bookshelf and one alternative offer");
			}
			if (level == 2) {
				helper.assertTrue(offers.stream().filter(offer -> offer.getResult().is(EnchantingItems.RUNES.get(0))
					|| offer.getResult().is(EnchantingItems.RUNES.get(1))).count() == 1, "Apprentices may sell exactly one of runes 1 and 2");
				helper.assertTrue(offers.stream().filter(offer -> offer.getResult().is(Items.EMERALD)
					&& offer.getBaseCostA().is(Items.BOOK)).count() == 1, "Apprentices also buy books");
			}
			if (level == 4) {
				helper.assertTrue(offers.stream().filter(offer -> offer.getResult().is(Items.CLOCK)
					|| offer.getResult().is(Items.COMPASS) || offer.getBaseCostA().is(Items.WRITABLE_BOOK)).count() == 2,
					"Experts may add only two level-four trades");
			}
			if (level == 5) {
				var attunement = this.enchantment(helper, ResourceKey.create(Registries.ENCHANTMENT, Wildways.id("attunement")));
				helper.assertTrue(offers.stream().filter(offer -> offer.getResult().is(Items.DYED_CANDLE.red())
					|| offer.getResult().is(Items.DYED_CANDLE.yellow())).count() == 1, "Masters must offer only one candle color");
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
		villager.setVillagerData(villager.getVillagerData().withProfession(helper.getLevel().registryAccess(), VillagerProfession.LIBRARIAN).withLevel(5));
		var offers = villager.getOffers();
		var attunement = this.enchantment(helper, ResourceKey.create(Registries.ENCHANTMENT, Wildways.id("attunement")));
		offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 9), new ItemStack(Items.BOOKSHELF), 12, 1, 0.05F));
		offers.add(new MerchantOffer(new ItemCost(Items.PAPER, 24), new ItemStack(Items.EMERALD), 16, 2, 0.05F));
		offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 5), new ItemStack(Items.CHISELED_BOOKSHELF), 12, 1, 0.05F));
		offers.add(new MerchantOffer(new ItemCost(Items.BOOK, 4), new ItemStack(Items.EMERALD), 12, 10, 0.05F));
		offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 10), new ItemStack(EnchantingItems.RUNES.get(0)), 12, 5, 0.05F));
		offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 20), new ItemStack(EnchantingItems.RUNES.get(1)), 12, 5, 0.05F));
		offers.add(new MerchantOffer(new ItemCost(Items.WRITABLE_BOOK, 2), new ItemStack(Items.EMERALD), 12, 15, 0.05F));
		offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 5), new ItemStack(Items.CLOCK), 12, 15, 0.05F));
		offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 5), new ItemStack(Items.COMPASS), 12, 15, 0.05F));
		Item otherCandle = offers.stream().anyMatch(offer -> offer.getResult().is(Items.DYED_CANDLE.red()))
			? Items.DYED_CANDLE.yellow() : Items.DYED_CANDLE.red();
		offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 3), new ItemStack(otherCandle), 12, 30, 0.05F));
		offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 18), new ItemStack(EnchantingItems.CATALYST), 12, 30, 0.05F));
		offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 18), this.book(helper, Enchantments.MENDING, 1), 12, 30, 0.05F));
		player.setItemInHand(InteractionHand.MAIN_HAND, this.book(helper, Enchantments.UNBREAKING, 1));
		UseEntityCallback.EVENT.invoker().interact(player, helper.getLevel(), InteractionHand.MAIN_HAND, villager, null);
		helper.assertTrue(offers.stream().filter(offer -> offer.getResult().is(Items.BOOKSHELF)).count() == 1,
			"Existing ordinary bookshelf sales must remain available without duplication");
		helper.assertTrue(offers.stream().filter(offer -> offer.getBaseCostA().is(Items.PAPER)
			|| offer.getResult().is(Items.CHISELED_BOOKSHELF)).count() == 1, "Only one novice alternative may remain");
		helper.assertTrue(offers.stream().filter(offer -> offer.getResult().is(EnchantingItems.RUNES.get(0))
			|| offer.getResult().is(EnchantingItems.RUNES.get(1))).count() == 1, "Only one apprentice rune may remain");
		helper.assertTrue(offers.stream().filter(offer -> offer.getResult().is(Items.CLOCK)
			|| offer.getResult().is(Items.COMPASS) || offer.getBaseCostA().is(Items.WRITABLE_BOOK)).count() == 2,
			"Existing experts must keep no more than two level-four trades");
		helper.assertTrue(offers.stream().filter(offer -> offer.getResult().is(Items.DYED_CANDLE.red())
			|| offer.getResult().is(Items.DYED_CANDLE.yellow())).count() == 1, "Only one master candle may remain");
		helper.assertTrue(offers.stream().anyMatch(offer -> offer.getResult().is(ModItems.BIOME_COMPASS) && offer.getBaseCostA().getCount() == 5), "Journeyman biome compass must cost five emeralds");
		helper.assertFalse(offers.stream().anyMatch(offer -> offer.getResult().is(EnchantingItems.CATALYST)),
			"Old catalyst offers must be removed");
		helper.assertTrue(offers.stream().filter(offer -> offer.getResult().is(Items.ENCHANTED_BOOK)).count() == 1,
			"Only the fixed Attunement book may be sold");
		helper.assertTrue(offers.stream().anyMatch(offer -> offer.getResult().is(Items.ENCHANTED_BOOK)
			&& offer.getResult().getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).getLevel(attunement) == 1
			&& offer.getBaseCostA().getCount() == 18), "Master librarians must sell Attunement for eighteen emeralds");
		helper.assertTrue(player.getMainHandItem().is(Items.ENCHANTED_BOOK), "Interacting must not donate the held book");
		UseEntityCallback.EVENT.invoker().interact(player, helper.getLevel(), InteractionHand.MAIN_HAND, villager, null);
		helper.assertTrue(offers.stream().filter(offer -> offer.getResult().is(Items.ENCHANTED_BOOK)).count() == 1,
			"Opening the same librarian twice must not duplicate the Attunement offer");
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

	private CraftingInput craftingInput(Item top, Item side, Item center) {
		Item brick = Items.CHISELED_STONE_BRICKS;
		return CraftingInput.of(3, 3, List.of(brick, top, brick, side, center, side, brick, top, brick).stream().map(ItemStack::new).toList());
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
		helper.assertTrue(menu.getSlot(2).getItem().getOrDefault(DataComponents.REPAIR_COST, 0) == 26, "Prior-work cost must increase by one instead of doubling");
		helper.succeed();
	}
}
