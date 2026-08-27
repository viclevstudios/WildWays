package com.viclev.wildways.client;

import com.viclev.wildways.ModItems;
import com.viclev.wildways.Wildways;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public final class WildwaysChestLootProvider extends SimpleFabricLootTableSubProvider {
	private static final ResourceKey<LootTable> QUARANTINE_GROUNDS_ANCHOR = table("chests/quarantine_grounds/anchor");
	private static final ResourceKey<LootTable> QUARANTINE_GROUNDS_GRAVE = table("chests/quarantine_grounds/grave");
	private static final ResourceKey<LootTable> QUARANTINE_GROUNDS_HOSPITAL = table("chests/quarantine_grounds/hospital");
	private static final ResourceKey<LootTable> QUARANTINE_GROUNDS_RUINS = table("chests/quarantine_grounds/ruins");
	private static final ResourceKey<LootTable> QUARANTINE_GROUNDS_SECRET = table("chests/quarantine_grounds/secret");

	public WildwaysChestLootProvider(
		FabricPackOutput output,
		CompletableFuture<HolderLookup.Provider> registriesFuture
	) {
		super(output, registriesFuture, LootContextParamSets.CHEST);
	}

	@Override
	public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> exporter) {
		exporter.accept(QUARANTINE_GROUNDS_ANCHOR, LootTable.lootTable().withPool(weightedPool(2, 4,
			entry(Items.IRON_INGOT, 10),
			entry(Items.IRON_NUGGET, 10),
			entry(Items.BREAD, 6),
			entry(ModItems.ENDERMITE_SHELL, 4),
			entry(Items.ENDER_PEARL, 2)
		)));

		exporter.accept(QUARANTINE_GROUNDS_GRAVE, LootTable.lootTable().withPool(weightedPool(2, 4,
			entry(Items.BONE, 10),
			entry(Items.ROTTEN_FLESH, 8),
			entry(Items.CANDLE, 5),
			entry(ModItems.ENDERMITE_SHELL, 4),
			entry(Items.ENDER_PEARL, 1)
		)));

		exporter.accept(QUARANTINE_GROUNDS_HOSPITAL, LootTable.lootTable().withPool(weightedPool(2, 3,
			entry(Items.BREAD, 10),
			entry(Items.HONEY_BOTTLE, 7),
			entry(Items.GLASS_BOTTLE, 6),
			entry(ModItems.ENDERMITE_SHELL, 4),
			entry(Items.GOLDEN_APPLE, 1)
		)));

		exporter.accept(QUARANTINE_GROUNDS_RUINS, LootTable.lootTable().withPool(weightedPool(3, 5,
			entry(Blocks.COBBLESTONE.asItem(), 10),
			entry(Blocks.STONE_BRICKS.asItem(), 8),
			entry(Items.IRON_NUGGET, 6),
			entry(ModItems.ENDERMITE_SHELL, 4),
			entry(Items.IRON_INGOT, 2)
		)));

		exporter.accept(QUARANTINE_GROUNDS_SECRET, LootTable.lootTable().withPool(weightedPool(2, 4,
			entry(Items.ENDER_PEARL, 10),
			entry(ModItems.ENDERMITE_SHELL, 8),
			entry(Items.AMETHYST_SHARD, 6),
			entry(Items.GOLD_INGOT, 4),
			entry(Items.DIAMOND, 2),
			entry(Items.ECHO_SHARD, 1)
		)));
	}

	private static ResourceKey<LootTable> table(String path) {
		return ResourceKey.create(Registries.LOOT_TABLE, Wildways.id(path));
	}

	private static LootPool.Builder weightedPool(int minRolls, int maxRolls, LootItem.Builder<?>... entries) {
		LootPool.Builder pool = LootPool.lootPool().setRolls(UniformGenerator.between(minRolls, maxRolls));
		for (LootItem.Builder<?> entry : entries) {
			pool.add(entry);
		}
		return pool;
	}

	private static LootItem.Builder<?> entry(Item item, int weight) {
		return LootItem.lootTableItem(item).setWeight(weight);
	}
}
