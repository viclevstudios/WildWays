package com.viclev.wildways;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.block.Blocks;

/** Guards against reintroducing separate gold items instead of updating vanilla equipment. */
public class GoldToolGameTests {
	@GameTest
	public void goldHeritageAndUpgradeRouteDetermineMiningSpeedAndDamage(GameTestHelper helper) {
		ItemStack gold = new ItemStack(Items.GOLDEN_PICKAXE);
		ItemStack amethyst = new ItemStack(ProgressionItems.AMETHYST_PICKAXE);
		ItemStack goldDiamond = this.upgrade(helper, gold, ProgressionItems.REFINEMENT_DIAMOND, Items.DIAMOND_PICKAXE);
		ItemStack goldVolcanite = this.upgrade(helper, gold, ProgressionItems.REFINEMENT_VOLCANITE, ModItems.VOLCANITE_PICKAXE);
		ItemStack amethystVolcanite = this.upgrade(helper, amethyst, ProgressionItems.REFINEMENT_VOLCANITE, ModItems.VOLCANITE_PICKAXE);
		ItemStack plainDiamond = new ItemStack(Items.DIAMOND_PICKAXE);
		this.assertToolStats(helper, goldDiamond, 8, 5, false);
		this.assertToolStats(helper, plainDiamond, 7.5F, 4.5, false);
		this.assertToolStats(helper, goldVolcanite, 7.5F, 4.7, true);
		this.assertToolStats(helper, amethystVolcanite, 7, 4.23, true);

		Item upgrade = Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE;
		ItemStack ingot = new ItemStack(Items.NETHERITE_INGOT);
		Item netherite = Items.NETHERITE_PICKAXE;
		this.assertToolStats(helper, this.upgrade(helper, goldDiamond, upgrade, ingot, netherite), 9, 6, false);
		this.assertToolStats(helper, this.upgrade(helper, plainDiamond, upgrade, ingot, netherite), 8.5F, 5.4, false);
		this.assertToolStats(helper, this.upgrade(helper, goldVolcanite, upgrade, ingot, netherite), 8.5F, 5.4, true);
		ItemStack upgraded = this.upgrade(helper, amethystVolcanite, upgrade, ingot, netherite);
		this.assertToolStats(helper, upgraded, 8, 4.8, true);
		helper.assertTrue(upgraded.getDestroySpeed(Blocks.DIRT.defaultBlockState()) == 1.5F,
			"The mining penalty must leave Amethyst's ineffective-block bonus intact");
		helper.succeed();
	}

	private void assertToolStats(GameTestHelper helper, ItemStack stack, float speed, double attack, boolean volcaniteReach) {
		var damage = new AttributeInstance(Attributes.ATTACK_DAMAGE, instance -> {});
		damage.setBaseValue(1);
		var reach = new AttributeInstance(Attributes.BLOCK_INTERACTION_RANGE, instance -> {});
		reach.setBaseValue(4.5);
		stack.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
			if (attribute.equals(Attributes.ATTACK_DAMAGE)) damage.addTransientModifier(modifier);
			if (attribute.equals(Attributes.BLOCK_INTERACTION_RANGE)) reach.addTransientModifier(modifier);
		});
		helper.assertTrue(stack.getDestroySpeed(Blocks.STONE.defaultBlockState()) == speed,
			"Effective mining speed must reflect the upgrade route");
		helper.assertTrue(Math.abs(damage.getValue() - attack) < 0.00001, "Attack damage must reflect the upgrade route");
		helper.assertTrue(reach.getValue() == (volcaniteReach ? 4.75 : 4.5), "Main-hand reach must reflect the upgrade route");
	}

	@GameTest
	public void vanillaGoldToolsUseTheRebalancedStats(GameTestHelper helper) {
		for (Item item : List.of(Items.GOLDEN_SWORD, Items.GOLDEN_PICKAXE, Items.GOLDEN_AXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_HOE)) {
			ItemStack stack = new ItemStack(item);
			helper.assertTrue(stack.getMaxDamage() == 400, "Vanilla gold tools must have 400 durability");
			helper.assertTrue(stack.get(DataComponents.ENCHANTABLE).value() == 20, "Vanilla gold tools must use the gold tier's enchantability");
			helper.assertTrue(stack.isValidRepairItem(new ItemStack(Items.GOLD_INGOT)), "Gold tools must still repair with gold ingots");
			String slot = BuiltInRegistries.ITEM.getKey(item).getPath().substring("golden_".length());
			helper.assertFalse(BuiltInRegistries.ITEM.containsKey(Wildways.id("gold_" + slot)), "Separate Wildways gold tools must not be registered");
		}
		helper.assertTrue(new ItemStack(Items.GOLDEN_PICKAXE).getDestroySpeed(Blocks.STONE.defaultBlockState()) == 7.0F
			&& new ItemStack(Items.GOLDEN_AXE).getDestroySpeed(Blocks.OAK_LOG.defaultBlockState()) == 7.0F
			&& new ItemStack(Items.GOLDEN_SHOVEL).getDestroySpeed(Blocks.DIRT.defaultBlockState()) == 7.0F
			&& new ItemStack(Items.GOLDEN_HOE).getDestroySpeed(Blocks.HAY_BLOCK.defaultBlockState()) == 7.0F,
			"Vanilla gold mining speed must be between iron and diamond");
		helper.succeed();
	}

	@GameTest
	public void refinementsProduceAndUpgradeVanillaGoldTools(GameTestHelper helper) {
		for (var pair : Map.of(Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.IRON_PICKAXE, Items.GOLDEN_PICKAXE,
			Items.IRON_AXE, Items.GOLDEN_AXE, Items.IRON_SHOVEL, Items.GOLDEN_SHOVEL, Items.IRON_HOE, Items.GOLDEN_HOE).entrySet()) {
			ItemStack gold = upgrade(helper, new ItemStack(pair.getKey()), ProgressionItems.REFINEMENT_GOLD, pair.getValue());
			String slot = BuiltInRegistries.ITEM.getKey(pair.getKey()).getPath().substring("iron_".length());
			upgrade(helper, gold, ProgressionItems.REFINEMENT_DIAMOND, BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace("diamond_" + slot)));
			upgrade(helper, gold, ProgressionItems.REFINEMENT_VOLCANITE, BuiltInRegistries.ITEM.getValue(Wildways.id("volcanite_" + slot)));
		}
		helper.succeed();
	}

	@GameTest
	public void creativeTabsGroupEquipmentAndSeparateTemplates(GameTestHelper helper) {
		List<Item> tools = tab(helper, CreativeModeTabs.TOOLS_AND_UTILITIES);
		List<Item> combat = tab(helper, CreativeModeTabs.COMBAT);
		List<Item> ingredients = tab(helper, CreativeModeTabs.INGREDIENTS);
		helper.assertTrue(Collections.indexOfSubList(tools, List.of(ProgressionItems.QUARTZ_SHOVEL, ProgressionItems.QUARTZ_PICKAXE,
			ProgressionItems.QUARTZ_AXE, ProgressionItems.QUARTZ_HOE, ProgressionItems.AMETHYST_SHOVEL,
			ProgressionItems.AMETHYST_PICKAXE, ProgressionItems.AMETHYST_AXE, ProgressionItems.AMETHYST_HOE)) >= 0,
			"Quartz and Amethyst tool groups must remain together in vanilla tool order");
		assertCombatMaterialGroups(helper, combat);
		for (Item sword : List.of(ProgressionItems.QUARTZ_SWORD, ProgressionItems.AMETHYST_SWORD, Items.GOLDEN_SWORD, ModItems.VOLCANITE_SWORD)) {
			helper.assertTrue(!tools.contains(sword) && Collections.frequency(combat, sword) == 1,
				"Each sword belongs in Combat exactly once");
		}
		for (Item tool : List.of(Items.GOLDEN_SHOVEL, Items.GOLDEN_PICKAXE, Items.GOLDEN_AXE, Items.GOLDEN_HOE)) {
			helper.assertTrue(Collections.frequency(tools, tool) == 1, "Vanilla gold tools must appear once in Tools");
		}
		for (Item template : ProgressionItems.REFINEMENTS) {
			helper.assertTrue(!tools.contains(template) && Collections.frequency(ingredients, template) == 1,
				"Refinement templates belong with ingredients, not between tools");
			helper.assertTrue(((SmithingTemplateItem) template).getAdditionalSlotEmptyIcons().isEmpty(),
				"Refinements must not suggest extra materials with addition-slot icons");
		}
		helper.assertTrue(Collections.frequency(combat, ModItems.VOLCANITE_AXE) == 1,
			"Volcanite axe must also be available alongside the other combat axes");
		helper.succeed();
	}

	private void assertCombatMaterialGroups(GameTestHelper helper, List<Item> combat) {
		List<String> materials = List.of("wooden", "stone", "leather", "copper", "chainmail", "iron",
			"quartz", "amethyst", "golden", "diamond", "volcanite", "netherite");
		int previousEnd = -1;
		for (String material : materials) {
			List<Integer> positions = java.util.stream.IntStream.range(0, combat.size()).filter(index -> {
				String path = BuiltInRegistries.ITEM.getKey(combat.get(index)).getPath();
				return path.startsWith(material + "_") && (path.endsWith("_sword") || path.endsWith("_spear") || path.endsWith("_axe")
					|| path.endsWith("_helmet") || path.endsWith("_chestplate") || path.endsWith("_leggings") || path.endsWith("_boots"));
			}).boxed().toList();
			helper.assertFalse(positions.isEmpty(), "Each combat material must remain available");
			int start = positions.getFirst();
			int end = positions.getLast();
			helper.assertTrue(start > previousEnd && end - start + 1 == positions.size(),
				"Weapons and armor must form one consecutive group for " + material);
			previousEnd = end;
		}
	}

	private List<Item> tab(GameTestHelper helper, ResourceKey<CreativeModeTab> key) {
		CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.getValue(key.identifier());
		tab.buildContents(new CreativeModeTab.ItemDisplayParameters(helper.getLevel().enabledFeatures(), false, helper.getLevel().registryAccess()));
		return tab.getDisplayItems().stream().map(ItemStack::getItem).toList();
	}

	private ItemStack upgrade(GameTestHelper helper, ItemStack base, Item template, Item expected) {
		return this.upgrade(helper, base, template, ItemStack.EMPTY, expected);
	}

	private ItemStack upgrade(GameTestHelper helper, ItemStack base, Item template, ItemStack addition, Item expected) {
		var input = new SmithingRecipeInput(new ItemStack(template), base.copy(), addition);
		var recipe = helper.getLevel().recipeAccess().getRecipeFor(RecipeType.SMITHING, input, helper.getLevel());
		helper.assertTrue(recipe.isPresent(), "Gold refinement recipe must be loaded");
		ItemStack output = recipe.orElseThrow().value().assemble(input);
		helper.assertTrue(output.is(expected), "Gold refinements must use vanilla gold equipment");
		return output;
	}
}
