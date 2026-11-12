package com.viclev.wildways;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;

public final class ModLootTables {
	private static final ResourceKey<LootTable> PIGLIN_BRUTE = vanilla("entities/piglin_brute");
	private static final ResourceKey<LootTable> BASTION_TREASURE = vanilla("chests/bastion_treasure");
	private static final java.util.Set<ResourceKey<LootTable>> BASTION_CHESTS = java.util.Set.of(
		vanilla("chests/bastion_bridge"), vanilla("chests/bastion_hoglin_stable"),
		BASTION_TREASURE
	);

	private ModLootTables() {
	}

	public static void initialize() {
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			// The vanilla trade rebalance pack supplies this table as a datapack.
			if (key.equals(BuiltInLootTables.JUNGLE_TEMPLE)) {
				tableBuilder.withPool(chancePool(ModItems.EYE_OF_THE_TIGER, 1, 1));
			}
			if (!source.isBuiltin()) {
				return;
			}

			if (isTrialVault(key)) {
				tableBuilder.withPool(LootPool.lootPool()
					.add(LootItem.lootTableItem(Items.BOOK).setWeight(1)
						.apply(EnchantRandomlyFunction.randomApplicableEnchantment(registries.lookupOrThrow(Registries.ENCHANTMENT))))
					.add(EmptyLootItem.emptyItem().setWeight(9)));
			}

			if (key.equals(PIGLIN_BRUTE)) {
				tableBuilder.withPool(chancePool(ModItems.EYE_OF_THE_BRUTE, 1, 2));
			}

			if (BASTION_CHESTS.contains(key)) {
				boolean treasure = key.equals(BASTION_TREASURE);
				tableBuilder.withPool(LootPool.lootPool()
					.add(LootItem.lootTableItem(ModItems.VOLCANITE)
						.setWeight(treasure ? 3 : 1)
						.apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 2))))
					.add(EmptyLootItem.emptyItem().setWeight(treasure ? 17 : 19)));
			}
		});
	}

	private static boolean isTrialVault(ResourceKey<LootTable> key) {
		String path = key.identifier().getPath();
		return path.equals("chests/trial_chambers/reward_common")
			|| path.equals("chests/trial_chambers/reward_ominous_common");
	}

	private static LootPool.Builder chancePool(net.minecraft.world.item.Item item, int itemWeight, int emptyWeight) {
		return LootPool.lootPool()
			.add(LootItem.lootTableItem(item).setWeight(itemWeight))
			.add(EmptyLootItem.emptyItem().setWeight(emptyWeight));
	}

	private static ResourceKey<LootTable> vanilla(String path) {
		return ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace(path));
	}
}
