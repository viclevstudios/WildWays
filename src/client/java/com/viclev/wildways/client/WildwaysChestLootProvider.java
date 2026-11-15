package com.viclev.wildways.client;

import com.viclev.wildways.ModItems;
import com.viclev.wildways.Wildways;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.entries.UniformContainerBase;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.List;

public final class WildwaysChestLootProvider extends SimpleFabricLootTableSubProvider {
	private static final ResourceKey<LootTable> QUARANTINE_GROUNDS_ANCHOR = table("chests/quarantine_grounds/anchor");
	private static final ResourceKey<LootTable> QUARANTINE_GROUNDS_GRAVE = table("chests/quarantine_grounds/grave");
	private static final ResourceKey<LootTable> QUARANTINE_GROUNDS_HOSPITAL = table("chests/quarantine_grounds/hospital");
	private static final ResourceKey<LootTable> QUARANTINE_GROUNDS_RUINS = table("chests/quarantine_grounds/ruins");
	private static final ResourceKey<LootTable> QUARANTINE_GROUNDS_SECRET = table("chests/quarantine_grounds/secret");
	private final CompletableFuture<HolderLookup.Provider> registriesFuture;

	public WildwaysChestLootProvider(
		FabricPackOutput output,
		CompletableFuture<HolderLookup.Provider> registriesFuture
	) {
		super(output, registriesFuture, LootContextParamSets.CHEST);
		this.registriesFuture = registriesFuture;
	}

	@Override
	public void run() {
		// Fabric's standalone provider calls generate with its own table exporter.
		throw new UnsupportedOperationException("Use the Fabric data provider with a CachedOutput");
	}

	@Override
	public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> exporter) {
		exporter.accept(QUARANTINE_GROUNDS_ANCHOR, LootTable.lootTable()
			.withPool(weightedPool(4, 8,
				entry(Items.IRON_NUGGET, 10, 1, 2),
				entry(Items.BREAD, 6, 1, 2),
				entry(Items.BAKED_POTATO, 6, 1, 2),
				entry(Items.GOLDEN_CARROT, 4, 1, 2),
				entry(ModItems.ENDERMITE_SHELL, 4),
				entry(Items.LANTERN, 3),
				entry(Items.BUNDLE, 2),
				entry(Items.BUCKET, 2)))
			.withPool(weightedPool(2, 4,
				entry(Items.ENDER_PEARL, 1),
				entry(Items.EMERALD, 1, 1, 2),
				entry(Items.IRON_INGOT, 2, 1, 2))));

		var ironArmor = LootTable.lootTable().setParamSet(LootContextParamSets.CHEST)
			.withPool(LootPool.lootPool()
				.add(entry(Items.IRON_HELMET, 1))
				.add(entry(Items.IRON_CHESTPLATE, 1))
				.add(entry(Items.IRON_LEGGINGS, 1))
				.add(entry(Items.IRON_BOOTS, 1)))
			.build();
		exporter.accept(QUARANTINE_GROUNDS_GRAVE, LootTable.lootTable()
			.withPool(weightedPool(4, 8,
				entry(Items.BONE, 5, 1, 2),
				entry(Items.ROTTEN_FLESH, 5, 1, 2),
				entry(Items.STRING, 4, 1, 2),
				entry(Items.GUNPOWDER, 4, 1, 2),
				entry(Items.COAL, 3, 1, 2),
				entry(Items.LEATHER, 3, 1, 2),
				entry(Items.CANDLE, 3),
				entry(Items.EXPERIENCE_BOTTLE, 3, 1, 2)))
			.withPool(weightedPool(2, 4,
				entry(ModItems.ENDERMITE_SHELL, 1),
				entry(Items.ENDER_PEARL, 1),
				entry(Items.GOLD_INGOT, 2, 1, 2),
				entry(Items.EMERALD, 1, 1, 2),
				NestedLootTable.inlineLootTable(ironArmor).setWeight(1))));

		exporter.accept(QUARANTINE_GROUNDS_HOSPITAL, LootTable.lootTable()
			.withPool(weightedPool(4, 8,
				entry(Items.BREAD, 6, 1, 3),
				entry(Items.BAKED_POTATO, 6, 1, 2),
				entry(Items.GOLDEN_CARROT, 4, 1, 2),
				entry(Items.HONEY_BOTTLE, 5, 1, 2),
				entry(Items.GLASS_BOTTLE, 6, 1, 2),
				entry(ModItems.ENDERMITE_SHELL, 4, 1, 2),
				entry(Items.GOLDEN_APPLE, 2))));

		var enchantments = registriesFuture.join().lookupOrThrow(Registries.ENCHANTMENT);
		exporter.accept(QUARANTINE_GROUNDS_RUINS, LootTable.lootTable()
			.withPool(weightedPool(4, 10,
				entry(Items.BREAD, 6, 1, 3),
				entry(Items.BAKED_POTATO, 6, 1, 2),
				entry(Items.GOLDEN_CARROT, 4, 1, 2),
				entry(Items.HONEY_BOTTLE, 3, 1, 2),
				entry(Items.GLASS_BOTTLE, 6, 1, 2),
				entry(ModItems.ENDERMITE_SHELL, 4, 1, 2),
				entry(Items.COBWEB, 4, 1, 2)))
			.withPool(weightedPool(2, 4,
				entry(ModItems.ENDERMITE_SHELL, 2, 1, 2),
				entry(Items.IRON_INGOT, 4, 1, 2),
				entry(Items.ENDER_PEARL, 2),
				entry(Items.GOLD_INGOT, 3, 1, 2),
				entry(Items.EMERALD, 2, 1, 2),
				entry(Items.IRON_SWORD, 1).apply(EnchantRandomlyFunction.randomApplicableEnchantment(enchantments)),
				entry(Items.IRON_AXE, 1).apply(EnchantRandomlyFunction.randomApplicableEnchantment(enchantments)),
				entry(Items.IRON_PICKAXE, 1).apply(EnchantRandomlyFunction.randomApplicableEnchantment(enchantments)),
				entry(Items.GOLDEN_APPLE, 1))));

		HolderSet<Enchantment> nestBooks = HolderSet.direct(List.of(
			enchantments.getOrThrow(Enchantments.UNBREAKING),
			enchantments.getOrThrow(Enchantments.PROTECTION),
			enchantments.getOrThrow(Enchantments.EFFICIENCY),
			enchantments.getOrThrow(Enchantments.FORTUNE),
			enchantments.getOrThrow(Enchantments.SILK_TOUCH),
			enchantments.getOrThrow(Enchantments.SHARPNESS),
			enchantments.getOrThrow(Enchantments.POWER)
		));
		exporter.accept(QUARANTINE_GROUNDS_SECRET, LootTable.lootTable()
			.withPool(LootPool.lootPool().add(entry(Items.BOOK, 1)
				.apply(EnchantRandomlyFunction.randomEnchantment().withOneOf(nestBooks))))
			.withPool(weightedPool(5, 8,
				entry(Items.ENDER_PEARL, 8),
				entry(ModItems.ENDERMITE_SHELL, 8, 1, 2),
				entry(Items.AMETHYST_SHARD, 6, 1, 2),
				entry(Items.IRON_BLOCK, 2),
				entry(Items.GOLD_INGOT, 4, 1, 2),
				entry(Items.DIAMOND, 3),
				entry(Items.EXPERIENCE_BOTTLE, 5, 1, 2)))
			.withPool(LootPool.lootPool().add(entry(ModItems.LOST_EYE, 1))));
	}

	private static ResourceKey<LootTable> table(String path) {
		return ResourceKey.create(Registries.LOOT_TABLE, Wildways.id(path));
	}

	private static LootPool.Builder weightedPool(int minRolls, int maxRolls, UniformContainerBase.Builder<?>... entries) {
		LootPool.Builder pool = LootPool.lootPool().setRolls(ContextIntProviders.between(minRolls, maxRolls));
		for (UniformContainerBase.Builder<?> entry : entries) {
			pool.add(entry);
		}
		return pool;
	}

	private static LootItem.Builder<?> entry(Item item, int weight) {
		return LootItem.lootTableItem(item).setWeight(weight);
	}

	private static LootItem.Builder<?> entry(Item item, int weight, int minCount, int maxCount) {
		return entry(item, weight).apply(SetItemCountFunction.setCount(ContextIntProviders.between(minCount, maxCount)));
	}
}
