package com.viclev.wildways;

import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

/** Exercises loaded spear components, enchantment tags and the complete smithing routes. */
public class SpearGameTests {
	@GameTest
	public void materialTraitsKeepVanillaSpearMechanics(GameTestHelper helper) {
		ItemStack iron = new ItemStack(Items.IRON_SPEAR);
		ItemStack quartz = new ItemStack(ProgressionItems.QUARTZ_SPEAR);
		ItemStack amethyst = new ItemStack(ProgressionItems.AMETHYST_SPEAR);
		ItemStack volcanite = new ItemStack(ModItems.VOLCANITE_SPEAR);
		helper.assertTrue(quartz.getMaxDamage() == 700 && quartz.getMaxDamage() > iron.getMaxDamage(),
			"Quartz spear must have the increased 700 durability");
		helper.assertTrue(amethyst.getMaxDamage() == 250 && volcanite.getMaxDamage() == 1400,
			"Amethyst and Volcanite spears must share their tool tier's durability");
		this.assertClose(helper, this.modifier(amethyst, Attributes.ATTACK_SPEED) - this.modifier(iron, Attributes.ATTACK_SPEED),
			0.2, "Amethyst must add 0.2 attacks per second");
		helper.assertTrue(amethyst.get(DataComponents.SWING_ANIMATION).duration() < iron.get(DataComponents.SWING_ANIMATION).duration(),
			"Amethyst must also shorten the visible stab animation");
		var ironRange = iron.get(DataComponents.ATTACK_RANGE);
		var volcaniteRange = volcanite.get(DataComponents.ATTACK_RANGE);
		this.assertClose(helper, volcaniteRange.maxReach() - ironRange.maxReach(), 0.25, "Volcanite must extend survival attack reach");
		this.assertClose(helper, volcaniteRange.maxCreativeReach() - ironRange.maxCreativeReach(), 0.25, "Volcanite must extend creative attack reach");
		this.assertClose(helper, volcaniteRange.minReach(), ironRange.minReach(), "The spear's close-range blind spot must remain");
		this.assertClose(helper, this.modifier(volcanite, Attributes.BLOCK_INTERACTION_RANGE), 0.25,
			"Volcanite spear must also retain the existing tool block reach bonus");
		helper.assertTrue(quartz.isValidRepairItem(new ItemStack(Items.QUARTZ))
			&& amethyst.isValidRepairItem(new ItemStack(Items.AMETHYST_SHARD))
			&& volcanite.isValidRepairItem(new ItemStack(ModItems.VOLCANITE)), "Each spear must accept its own repair material");
		var lunge = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LUNGE);
		for (ItemStack spear : List.of(quartz, amethyst, volcanite)) {
			helper.assertTrue(spear.is(ItemTags.SPEARS) && lunge.value().isSupportedItem(spear), "Custom spears must support spear enchantments");
			helper.assertTrue(spear.has(DataComponents.KINETIC_WEAPON) && spear.has(DataComponents.PIERCING_WEAPON),
				"Custom spears must support charged attacks and piercing stabs");
		}
		helper.succeed();
	}

	@GameTest
	public void vanillaGoldSpearSitsBetweenIronAndDiamond(GameTestHelper helper) {
		ItemStack iron = new ItemStack(Items.IRON_SPEAR);
		ItemStack gold = new ItemStack(Items.GOLDEN_SPEAR);
		ItemStack diamond = new ItemStack(Items.DIAMOND_SPEAR);
		helper.assertTrue(iron.getMaxDamage() < gold.getMaxDamage() && gold.getMaxDamage() == 400 && gold.getMaxDamage() < diamond.getMaxDamage(),
			"The vanilla golden spear must use 400 durability");
		for (Holder<Attribute> attribute : List.of(Attributes.ATTACK_DAMAGE, Attributes.ATTACK_SPEED)) {
			double min = Math.min(this.modifier(iron, attribute), this.modifier(diamond, attribute));
			double max = Math.max(this.modifier(iron, attribute), this.modifier(diamond, attribute));
			helper.assertTrue(this.modifier(gold, attribute) > min && this.modifier(gold, attribute) < max,
				"Gold's damage and attack speed must be between iron and diamond");
		}
		float multiplier = gold.get(DataComponents.KINETIC_WEAPON).damageMultiplier();
		helper.assertTrue(multiplier > iron.get(DataComponents.KINETIC_WEAPON).damageMultiplier()
			&& multiplier < diamond.get(DataComponents.KINETIC_WEAPON).damageMultiplier(), "Gold's charged damage must be between iron and diamond");
		helper.assertTrue(gold.isValidRepairItem(new ItemStack(Items.GOLD_INGOT)), "The revised gold spear must still repair with gold");
		helper.succeed();
	}

	@GameTest
	public void spearsFollowAllRefinementAndNetheriteRoutes(GameTestHelper helper) {
		for (Item template : List.of(ProgressionItems.REFINEMENT_QUARTZ, ProgressionItems.REFINEMENT_AMETHYST, ProgressionItems.REFINEMENT_GOLD)) {
			Item expected = template == ProgressionItems.REFINEMENT_QUARTZ ? ProgressionItems.QUARTZ_SPEAR
				: template == ProgressionItems.REFINEMENT_AMETHYST ? ProgressionItems.AMETHYST_SPEAR : Items.GOLDEN_SPEAR;
			ItemStack middle = this.upgrade(helper, new ItemStack(Items.IRON_SPEAR), template, ItemStack.EMPTY, expected);
			for (Item upperTemplate : List.of(ProgressionItems.REFINEMENT_DIAMOND, ProgressionItems.REFINEMENT_VOLCANITE)) {
				Item upper = upperTemplate == ProgressionItems.REFINEMENT_DIAMOND ? Items.DIAMOND_SPEAR : ModItems.VOLCANITE_SPEAR;
				ItemStack endgame = this.upgrade(helper, middle, upperTemplate, ItemStack.EMPTY, upper);
				this.upgrade(helper, endgame, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, new ItemStack(Items.NETHERITE_INGOT), Items.NETHERITE_SPEAR);
			}
		}
		helper.succeed();
	}

	@GameTest
	public void amethystSpearUpgradesPreserveNamesEnchantmentsAndSpeed(GameTestHelper helper) {
		ItemStack amethyst = new ItemStack(ProgressionItems.AMETHYST_SPEAR);
		amethyst.set(DataComponents.CUSTOM_NAME, Component.literal("Trailfinder"));
		var unbreaking = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING);
		amethyst.enchant(unbreaking, 2);
		ItemStack volcanite = this.upgrade(helper, amethyst, ProgressionItems.REFINEMENT_VOLCANITE, ItemStack.EMPTY, ModItems.VOLCANITE_SPEAR);
		ItemStack netherite = this.upgrade(helper, volcanite, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
			new ItemStack(Items.NETHERITE_INGOT), Items.NETHERITE_SPEAR);
		for (ItemStack upgraded : List.of(volcanite, netherite)) {
			helper.assertTrue(upgraded.getHoverName().getString().equals("Trailfinder")
				&& EnchantmentHelper.getItemEnchantmentLevel(unbreaking, upgraded) == 2,
				"Spear upgrades must preserve custom names and enchantments");
			helper.assertTrue(ProgressionItems.hasAmethystHeritage(upgraded), "Spear upgrades must retain Amethyst heritage");
			this.assertClose(helper, this.modifier(upgraded, Attributes.ATTACK_SPEED)
				- this.modifier(new ItemStack(upgraded.getItem()), Attributes.ATTACK_SPEED), 0.2,
				"Upgraded Amethyst spears must keep their speed bonus exactly once");
		}
		helper.succeed();
	}

	private ItemStack upgrade(GameTestHelper helper, ItemStack base, Item template, ItemStack addition, Item expected) {
		SmithingRecipeInput input = new SmithingRecipeInput(new ItemStack(template), base.copy(), addition);
		var recipe = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.SMITHING, input, helper.getLevel());
		helper.assertTrue(recipe.isPresent(), "Spear smithing recipe must be loaded and match its ingredients");
		ItemStack output = recipe.orElseThrow().value().assemble(input);
		helper.assertTrue(output.is(expected), "Spear smithing recipe must produce the matching tier");
		return output;
	}

	private double modifier(ItemStack stack, Holder<Attribute> expected) {
		double[] result = {0};
		stack.forEachModifier(EquipmentSlotGroup.MAINHAND, (attribute, modifier, display) -> {
			if (attribute.equals(expected)) result[0] += modifier.amount();
		});
		return result[0];
	}

	private void assertClose(GameTestHelper helper, double actual, double expected, String message) {
		helper.assertTrue(Math.abs(actual - expected) < 0.00001, message);
	}
}
