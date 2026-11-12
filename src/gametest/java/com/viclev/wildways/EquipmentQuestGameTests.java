package com.viclev.wildways;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Map;

public class EquipmentQuestGameTests {
	@GameTest
	public void diamondCraftingAndRefinementGiveDifferentBonuses(GameTestHelper helper) {
		var level = helper.getLevel();
		ItemStack gem = new ItemStack(Items.DIAMOND);
		CraftingInput input = CraftingInput.of(3, 3,
			List.of(gem, ItemStack.EMPTY, gem, gem, gem, gem, gem, gem, gem));
		var recipe = level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, level).orElseThrow();
		ItemStack crafted = recipe.value().assemble(input);
		helper.assertTrue(ItemStack.isSameItemSameComponents(crafted, new ItemStack(Items.DIAMOND_CHESTPLATE)),
			"The original crafting pattern must yield a standard diamond chestplate");

		ItemStack quartz = refineDiamond(helper, ProgressionItems.QUARTZ_CHESTPLATE);
		ItemStack amethyst = refineDiamond(helper, ProgressionItems.AMETHYST_CHESTPLATE);
		ItemStack gold = refineDiamond(helper, Items.GOLDEN_CHESTPLATE);
		helper.assertTrue(quartz.getMaxDamage() == Math.round(crafted.getMaxDamage() * 1.25F),
			"Quartz refinement must add durability");
		assertModifierBonus(helper, amethyst, crafted, EquipmentSlot.CHEST, Attributes.MOVEMENT_SPEED, 0.0025);
		assertModifierBonus(helper, gold, crafted, EquipmentSlot.CHEST, Attributes.ARMOR_TOUGHNESS, 1.0);
		assertModifierBonus(helper, refineDiamond(helper, Items.GOLDEN_SWORD), new ItemStack(Items.DIAMOND_SWORD),
			EquipmentSlot.MAINHAND, Attributes.ATTACK_DAMAGE, 0.5);

		SmithingRecipeInput netheriteInput = new SmithingRecipeInput(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
			quartz, new ItemStack(Items.NETHERITE_INGOT));
		ItemStack netherite = level.recipeAccess().getRecipeFor(RecipeType.SMITHING, netheriteInput, level)
			.orElseThrow().value().assemble(netheriteInput);
		helper.assertTrue(netherite.is(Items.NETHERITE_CHESTPLATE)
				&& netherite.getMaxDamage() == Math.round(new ItemStack(Items.NETHERITE_CHESTPLATE).getMaxDamage() * 1.25F),
			"Netherite must apply the Quartz bonus to Netherite durability");
		helper.succeed();
	}

	private static ItemStack refineDiamond(GameTestHelper helper, Item base) {
		var level = helper.getLevel();
		SmithingRecipeInput input = new SmithingRecipeInput(new ItemStack(ProgressionItems.REFINEMENT_DIAMOND),
			new ItemStack(base), ItemStack.EMPTY);
		return level.recipeAccess().getRecipeFor(RecipeType.SMITHING, input, level).orElseThrow().value().assemble(input);
	}

	private static void assertModifierBonus(GameTestHelper helper, ItemStack refined, ItemStack plain,
			EquipmentSlot slot, Holder<Attribute> attribute, double expected) {
		helper.assertTrue(refined.is(plain.getItem())
				&& Math.abs(modifier(refined, slot, attribute) - modifier(plain, slot, attribute) - expected) < 0.00001,
			"Refinement must add the expected bonus when equipped");
	}

	private static double modifier(ItemStack stack, EquipmentSlot slot, Holder<Attribute> attribute) {
		double[] amount = {0};
		stack.forEachModifier(slot, (type, modifier) -> {
			if (type.equals(attribute)) amount[0] += modifier.amount();
		});
		return amount[0];
	}

	@GameTest
	public void copperBreakCountsEvenWhenTheToolBreaks(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		GameType.SURVIVAL.updatePlayerAbilities(player.getAbilities());
		var quest = Wildways.id("quests/mine_with_copper");
		BlockPos stone = new BlockPos(1, 2, 1);
		helper.setBlock(stone, Blocks.STONE);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
		helper.assertTrue(player.gameMode.destroyBlock(helper.absolutePos(stone)), "The iron pickaxe should break the block");
		helper.assertTrue(!QuestBookItem.readProgress(player).get(quest).done(), "Iron should not satisfy the copper quest");

		helper.setBlock(stone, Blocks.STONE);
		ItemStack copper = new ItemStack(Items.COPPER_PICKAXE);
		copper.setDamageValue(copper.getMaxDamage() - 1);
		player.setItemInHand(InteractionHand.MAIN_HAND, copper);
		helper.assertTrue(player.gameMode.destroyBlock(helper.absolutePos(stone)), "The copper pickaxe should break the block");
		helper.assertTrue(player.getMainHandItem().isEmpty(), "The last use should break the pickaxe");
		helper.assertTrue(QuestBookItem.readProgress(player).get(quest).done(), "The final block break should still count");
		helper.succeed();
	}

	@GameTest
	public void smithingAndFletchingCountTakenResults(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		var gold = Wildways.id("quests/refine_gold");
		var iron = Wildways.id("quests/refine_iron");
		var diamond = Wildways.id("quests/refine_diamond");
		var smithing = new SmithingMenu(1, player.getInventory());
		smithing.getSlot(0).set(new ItemStack(ProgressionItems.REFINEMENT_GOLD));
		smithing.getSlot(1).set(new ItemStack(Items.IRON_PICKAXE));
		helper.assertTrue(smithing.getSlot(3).getItem().is(Items.GOLDEN_PICKAXE), "Gold should be available in the result slot");
		helper.assertTrue(!QuestBookItem.readProgress(player).get(gold).done(), "A smithing preview should not count");
		smithing.quickMoveStack(player, 3);
		var progress = QuestBookItem.readProgress(player);
		helper.assertTrue(progress.get(iron).done() && progress.get(gold).done(), "Refining iron to gold should complete both quests");
		helper.assertTrue(!progress.get(Wildways.id("quests/craft_gold_refinement")).done(),
			"Using a refinement should not count as crafting the refinement");
		smithing.getSlot(0).set(new ItemStack(ProgressionItems.REFINEMENT_DIAMOND));
		smithing.getSlot(1).set(new ItemStack(Items.GOLDEN_PICKAXE));
		smithing.quickMoveStack(player, 3);
		helper.assertTrue(QuestBookItem.readProgress(player).get(diamond).done(), "Taking the diamond upgrade should count");

		var fletching = new FletchingTableMenu(2, player.getInventory());
		for (var arrow : Map.<Item, String>of(Items.TURTLE_SCUTE, "fletch_turtle_arrow",
			Items.PHANTOM_MEMBRANE, "fletch_range_arrow", Items.TNT, "fletch_explosive_arrow").entrySet()) {
			fletching.getSlot(1).set(new ItemStack(Items.ARROW));
			fletching.getSlot(2).set(new ItemStack(arrow.getKey()));
			var quest = Wildways.id("quests/" + arrow.getValue());
			helper.assertTrue(!QuestBookItem.readProgress(player).get(quest).done(), "An arrow preview should not count");
			fletching.quickMoveStack(player, 3);
			helper.assertTrue(QuestBookItem.readProgress(player).get(quest).done(), "Taking the special arrow should count");
		}
		helper.assertTrue(QuestBookItem.readProgress(player).get(Wildways.id("quests/fletch_arrows")).done(),
			"A special arrow should also count as any arrow");
		helper.succeed();
	}

	@GameTest
	public void amethystQuestsRequireDamageAndWornArmor(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		var armor = Wildways.id("quests/wear_amethyst_armor");
		var combat = Wildways.id("quests/amethyst_combat");
		player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ProgressionItems.AMETHYST_HELMET));
		player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ProgressionItems.AMETHYST_CHESTPLATE));
		player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ProgressionItems.AMETHYST_LEGGINGS));
		player.getInventory().add(new ItemStack(ProgressionItems.AMETHYST_BOOTS));
		CriteriaTriggers.TICK.trigger(player);
		helper.assertTrue(!QuestBookItem.readProgress(player).get(armor).done(), "Carrying the last piece should not count as wearing it");
		player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ProgressionItems.AMETHYST_BOOTS));
		CriteriaTriggers.TICK.trigger(player);
		helper.assertTrue(QuestBookItem.readProgress(player).get(armor).done(), "Wearing all four pieces should count");

		var cow = helper.spawn(EntityTypes.COW, new BlockPos(2, 2, 2));
		var projectile = helper.spawn(EntityTypes.ARROW, new BlockPos(1, 2, 1));
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ProgressionItems.AMETHYST_AXE));
		cow.hurtServer(helper.getLevel(), helper.getLevel().damageSources().arrow(projectile, player), 2.0F);
		helper.assertTrue(!QuestBookItem.readProgress(player).get(combat).done(), "Holding amethyst while an arrow hits should not count");
		var directTarget = helper.spawn(EntityTypes.COW, new BlockPos(2, 2, 3));
		helper.assertTrue(directTarget.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 2.0F),
			"The direct attack should deal damage");
		helper.assertTrue(QuestBookItem.readProgress(player).get(combat).done(), "Damage from the amethyst tool should count");

		ServerPlayer spearPlayer = helper.makeMockServerPlayerInLevel();
		var spearTarget = helper.spawn(EntityTypes.COW, new BlockPos(3, 2, 2));
		spearPlayer.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ProgressionItems.AMETHYST_SPEAR));
		spearPlayer.startUsingItem(InteractionHand.OFF_HAND);
		helper.assertTrue(spearPlayer.stabAttack(EquipmentSlot.OFFHAND, spearTarget, 20.0F, true, false, false),
			"The offhand spear should deal lethal damage");
		helper.assertTrue(QuestBookItem.readProgress(spearPlayer).get(combat).done(), "The offhand spear kill should count");
		helper.succeed();
	}
}
