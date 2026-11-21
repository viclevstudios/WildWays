package com.viclev.wildways;

import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;

public class ArmorProgressionGameTests {
	@GameTest
	public void goldAndVolcaniteHaveTheirFullSetProtectionWhenEquipped(GameTestHelper helper) {
		var wearer = helper.spawn(EntityTypes.ARMOR_STAND, new BlockPos(1, 1, 1));
		wearer.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
		wearer.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
		wearer.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS));
		wearer.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS));

		helper.runAfterDelay(2, () -> {
			helper.assertTrue(wearer.getArmorValue() == 18, "All equipped gold armor bonuses must stack");
			wearer.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
		});
		helper.runAfterDelay(4, () -> {
			helper.assertTrue(wearer.getArmorValue() == 10, "Removing gold chest armor must leave the other slot bonuses");
			wearer.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.VOLCANITE_HELMET));
			wearer.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.VOLCANITE_CHESTPLATE));
			wearer.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.VOLCANITE_LEGGINGS));
			wearer.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.VOLCANITE_BOOTS));
		});
		helper.runAfterDelay(6, () -> {
			helper.assertTrue(wearer.getArmorValue() == 19 && wearer.getAttributeValue(Attributes.ARMOR_TOUGHNESS) == 2,
				"Volcanite must equip with 19 armor and 2 toughness");
			helper.succeed();
		});
	}

	@GameTest
	public void armorHeritageSurvivesBothRoutesAndGoldGivesMoreToughness(GameTestHelper helper) {
		for (Item template : List.of(ProgressionItems.REFINEMENT_DIAMOND, ProgressionItems.REFINEMENT_VOLCANITE)) {
			ItemStack quartz = this.refine(helper, new ItemStack(ProgressionItems.QUARTZ_CHESTPLATE), template, ItemStack.EMPTY);
			ItemStack amethyst = this.refine(helper, new ItemStack(ProgressionItems.AMETHYST_CHESTPLATE), template, ItemStack.EMPTY);
			ItemStack gold = this.refine(helper, new ItemStack(Items.GOLDEN_CHESTPLATE), template, ItemStack.EMPTY);
			boolean volcanite = template == ProgressionItems.REFINEMENT_VOLCANITE;
			Item upperTier = volcanite ? ModItems.VOLCANITE_CHESTPLATE : Items.DIAMOND_CHESTPLATE;
			helper.assertTrue(quartz.is(upperTier) && amethyst.is(upperTier) && gold.is(upperTier), "Refinements must produce the selected tier");
			this.assertHeritage(helper, quartz, amethyst, gold, volcanite ? 1 : 2, volcanite);

			ItemStack ingot = new ItemStack(Items.NETHERITE_INGOT);
			Item upgrade = Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE;
			ItemStack netheriteQuartz = this.refine(helper, quartz, upgrade, ingot);
			ItemStack netheriteAmethyst = this.refine(helper, amethyst, upgrade, ingot);
			ItemStack netheriteGold = this.refine(helper, gold, upgrade, ingot);
			helper.assertTrue(netheriteQuartz.is(Items.NETHERITE_CHESTPLATE) && netheriteAmethyst.is(Items.NETHERITE_CHESTPLATE)
				&& netheriteGold.is(Items.NETHERITE_CHESTPLATE), "Both branches must upgrade to Netherite");
			this.assertHeritage(helper, netheriteQuartz, netheriteAmethyst, netheriteGold, volcanite ? 2.5 : 3, volcanite);
		}
		helper.succeed();
	}

	private ItemStack refine(GameTestHelper helper, ItemStack base, Item template, ItemStack addition) {
		var input = new SmithingRecipeInput(new ItemStack(template), base, addition);
		var recipe = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.SMITHING, input, helper.getLevel());
		helper.assertTrue(recipe.isPresent(), "Chestplate recipe must match the smithing inputs");
		return recipe.orElseThrow().value().assemble(input);
	}

	private void assertHeritage(GameTestHelper helper, ItemStack quartz, ItemStack amethyst, ItemStack gold, double goldToughness, boolean volcanite) {
		int baseDurability = new ItemStack(quartz.getItem()).getMaxDamage();
		helper.assertTrue(ProgressionItems.hasQuartzHeritage(quartz) && quartz.getMaxDamage() == Math.round(baseDurability * 1.25F),
			"Quartz must apply its durability bonus once to the current tier");
		helper.assertTrue(ProgressionItems.hasAmethystHeritage(amethyst), "Amethyst mobility heritage must be retained");
		helper.assertTrue(this.modifier(gold, Attributes.ARMOR_TOUGHNESS) == goldToughness
			&& this.modifier(quartz, Attributes.ARMOR_TOUGHNESS) == goldToughness - 0.5
			&& this.modifier(amethyst, Attributes.ARMOR_TOUGHNESS) == goldToughness - 0.5,
			"The upgrade route and Gold heritage must determine the equipped toughness");
		for (ItemStack stack : List.of(quartz, amethyst, gold)) {
			helper.assertTrue(this.modifier(stack, Attributes.BLOCK_INTERACTION_RANGE) == (volcanite ? 0.25 : 0),
				"Volcanite block reach must carry through to Netherite regardless of material heritage");
		}
	}

	private double modifier(ItemStack stack, Holder<Attribute> expected) {
		double[] amount = {0};
		stack.forEachModifier(EquipmentSlot.CHEST, (attribute, modifier) -> {
			if (attribute.equals(expected)) amount[0] += modifier.amount();
		});
		return amount[0];
	}
}
