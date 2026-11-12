package com.viclev.wildways;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PlantQuality {
	public static final DataComponentType<Unit> LEVEL_TWO = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
		Wildways.id("level_two_crop"), DataComponentType.<Unit>builder().persistent(Unit.CODEC).build());
	public static final FoodProperties CAKE_SLICE = food(3, 0.6F);

	private static final Map<Item, FoodProperties> FOOD = Map.ofEntries(
		Map.entry(Items.BREAD, food(6, 8)), Map.entry(Items.COOKIE, food(3, 0.6F)),
		Map.entry(Items.CARROT, food(4, 5)), Map.entry(Items.GOLDEN_CARROT, food(7, 16)),
		Map.entry(Items.POTATO, food(2, 1)), Map.entry(Items.BAKED_POTATO, food(6, 8)),
		Map.entry(Items.RABBIT_STEW, food(12, 15)), Map.entry(Items.BEETROOT, food(2, 2)),
		Map.entry(Items.BEETROOT_SOUP, food(9, 12)), Map.entry(Items.MELON_SLICE, food(3, 2)),
		Map.entry(Items.PUMPKIN_PIE, food(10, 7)), Map.entry(Items.SWEET_BERRIES, food(3, 1))
	);
	private static final Set<Item> OTHER_CRAFTING_RESULTS = Set.of(Items.CAKE, Items.MELON, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS);
	private static final Set<Item> CRAFTING_INGREDIENTS = Set.of(Items.WHEAT, Items.CARROT, Items.POTATO,
		Items.BAKED_POTATO, Items.BEETROOT, Items.MELON_SLICE, Items.PUMPKIN);

	private PlantQuality() {
	}

	public static void initialize() {
		LootTableEvents.MODIFY_DROPS.register((table, context, drops) -> {
			BlockState state = context.getOptional(LootContextParams.BLOCK_STATE);
			if (state != null && state.getValueOrElse(PlantGrowth.QUALITY, PlantGrowth.NORMAL) != PlantGrowth.NORMAL) {
				markHarvest(state, drops, !context.hasParameter(LootContextParams.EXPLOSION_RADIUS));
				if (state.getValue(PlantGrowth.QUALITY) == PlantGrowth.LEVEL_TWO_SEEDS
					&& context.getOptional(LootContextParams.THIS_ENTITY) instanceof ServerPlayer player
					&& drops.stream().anyMatch(stack -> stack.has(LEVEL_TWO))) {
					QuestBookItem.awardQuest(player, Wildways.id("quests/gold_ripen"));
					var origin = context.getOptional(LootContextParams.ORIGIN);
					if (origin != null && context.getLevel().getBlockState(BlockPos.containing(origin).below()).is(PlantBlocks.COMPOST_FARMLAND)) {
						QuestBookItem.awardQuest(player, Wildways.id("quests/gold_ripen_compost"));
					}
				}
			}
		});
	}

	public static ItemStack mark(ItemStack stack) {
		stack.set(LEVEL_TWO, Unit.INSTANCE);
		FoodProperties properties = FOOD.get(stack.getItem());
		if (properties != null) {
			stack.set(DataComponents.FOOD, properties);
		}
		return stack;
	}

	public static void inheritCraftingQuality(CraftingInput input, ItemStack result) {
		if (!FOOD.containsKey(result.getItem()) && !OTHER_CRAFTING_RESULTS.contains(result.getItem())) {
			return;
		}
		boolean hasCrop = false;
		for (ItemStack stack : input.items()) {
			if (CRAFTING_INGREDIENTS.contains(stack.getItem())) {
				if (!stack.has(LEVEL_TWO)) {
					return;
				}
				hasCrop = true;
			}
		}
		if (hasCrop) {
			mark(result);
		}
	}

	private static void markHarvest(BlockState state, List<ItemStack> drops, boolean guaranteeSeed) {
		int quality = state.getValue(PlantGrowth.QUALITY);
		Item seed = seedFor(state);
		boolean foundSeed = false;
		for (int i = 0, size = drops.size(); i < size; i++) {
			ItemStack drop = drops.get(i);
			if (seed != null && drop.is(seed)) {
				if (quality == PlantGrowth.LEVEL_TWO || seed != Items.CARROT && seed != Items.POTATO && seed != Items.SWEET_BERRIES) {
					mark(drop);
				} else if (!foundSeed) {
					if (drop.getCount() == 1) {
						mark(drop);
					} else {
						drops.add(mark(drop.split(1)));
					}
				}
				foundSeed = true;
			} else if (quality == PlantGrowth.LEVEL_TWO && (drop.is(Items.WHEAT) || drop.is(Items.BEETROOT)
				|| drop.is(Items.MELON) || drop.is(Items.MELON_SLICE) || drop.is(Items.PUMPKIN))) {
				mark(drop);
			}
		}
		if (seed != null && !foundSeed && guaranteeSeed && PlantGrowth.isMature(state)) {
			drops.add(mark(new ItemStack(seed)));
		}
	}

	private static Item seedFor(BlockState state) {
		if (state.is(Blocks.WHEAT)) return Items.WHEAT_SEEDS;
		if (state.is(Blocks.BEETROOTS)) return Items.BEETROOT_SEEDS;
		if (state.is(Blocks.CARROTS)) return Items.CARROT;
		if (state.is(Blocks.POTATOES)) return Items.POTATO;
		if (state.is(Blocks.SWEET_BERRY_BUSH)) return Items.SWEET_BERRIES;
		if (state.is(Blocks.MELON_STEM) || state.is(Blocks.ATTACHED_MELON_STEM)) return Items.MELON_SEEDS;
		if (state.is(Blocks.PUMPKIN_STEM) || state.is(Blocks.ATTACHED_PUMPKIN_STEM)) return Items.PUMPKIN_SEEDS;
		return null;
	}

	private static FoodProperties food(int hunger, float saturation) {
		return new FoodProperties(hunger, saturation, false);
	}
}
