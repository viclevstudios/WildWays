package com.viclev.wildways;

import net.fabricmc.fabric.api.loot.v3.FabricLootTableBuilder;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;

public final class ModLootTables {
	private static final ResourceKey<LootTable> IGLOO = vanilla("chests/igloo_chest");
	private static final ResourceKey<LootTable> SNOWY_VILLAGE = vanilla("chests/village/village_snowy_house");
	private static final ResourceKey<LootTable> TRAIL_RUINS_COMMON = vanilla("archaeology/trail_ruins_common");
	private static final ResourceKey<LootTable> PIGLIN_BRUTE = vanilla("entities/piglin_brute");

	private ModLootTables() {
	}

	public static void initialize() {
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (!source.isBuiltin()) {
				return;
			}

			if (key.equals(IGLOO)) {
				tableBuilder.withPool(LootPool.lootPool().add(LootItem.lootTableItem(ModItems.EYE_OF_ICE)));
			} else if (key.equals(SNOWY_VILLAGE)) {
				tableBuilder.withPool(chancePool(ModItems.EYE_OF_ICE, 1, 9));
			} else if (key.equals(PIGLIN_BRUTE)) {
				tableBuilder.withPool(chancePool(ModItems.EYE_OF_THE_BRUTE, 1, 2));
			} else if (key.equals(TRAIL_RUINS_COMMON)) {
				((FabricLootTableBuilder)(Object)tableBuilder).modifyPools(
					pool -> pool.add(LootItem.lootTableItem(ModItems.LOST_EYE).setWeight(2))
				);
			}
		});
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
