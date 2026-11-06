package com.viclev.wildways;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.LocationPredicate;
import net.minecraft.advancements.predicates.NbtPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import net.minecraft.world.level.storage.loot.functions.FilteredFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

import java.util.Optional;
import java.util.Set;

public final class StructureMaps {
	public static final String PATROL_CAPTAIN_TAG = "wildways:patrol_captain";
	private static final ResourceKey<LootTable> PILLAGER_LOOT = ResourceKey.create(Registries.LOOT_TABLE,
		Identifier.withDefaultNamespace("entities/pillager"));
	private static final ResourceKey<LootTable> QUARANTINE_ANCHOR = wildwaysLoot("chests/quarantine_grounds/anchor");
	private static final Set<ResourceKey<LootTable>> DUNGEON_CHESTS = Set.of(BuiltInLootTables.SIMPLE_DUNGEON,
		wildwaysLoot("chests/dungeon_zombie"), wildwaysLoot("chests/dungeon_skeleton"), wildwaysLoot("chests/dungeon_spider"));
	private static final TagKey<Structure> IGLOOS = mapTarget("igloo");
	private static final TagKey<Structure> TRAIL_RUINS = mapTarget("trail_ruins");
	private static final TagKey<Structure> PILLAGER_OUTPOSTS = mapTarget("pillager_outpost");
	private static final float FISHING_MAP_CHANCE = 1.0F / 7.0F;

	private StructureMaps() {
	}

	public static void initialize() {
		Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, Wildways.id("structure_map"), StructureMapLootFunction.CODEC);
		DungeonLocations.initialize();
		LootTableEvents.MODIFY.register((key, table, source, registries) -> {
			// 26.3 can report bundled table overrides as data packs.
			if (source == LootTableSource.REPLACED) {
				return;
			}
			if (DUNGEON_CHESTS.contains(key)) {
				table.withPool(mapPool(Items.BURIED_MINESHAFT_MAP,
					locate(registries, StructureTags.ON_MINESHAFT_MAPS, MapDecorationTypes.MINESHAFT)).when(chance(0.25F)));
			} else if (key.equals(BuiltInLootTables.ABANDONED_MINESHAFT)
				|| key.equals(BuiltInLootTables.ABANDONED_CAMP_COMMON_CHEST)
				|| key.equals(BuiltInLootTables.ABANDONED_CAMP_SECRET_CHEST)) {
				table.withPool(mapPool(ModItems.DUNGEON_MAP, Optional.empty()).when(chance(0.25F)));
			} else if (key.equals(QUARANTINE_ANCHOR)) {
				table.withPool(mapPool(ModItems.TRAIL_RUINS_MAP, locate(registries, TRAIL_RUINS, MapDecorationTypes.RED_X)));
			} else if (key.equals(BuiltInLootTables.VILLAGE_SNOWY_HOUSE)) {
				table.withPool(mapPool(ModItems.IGLOO_MAP, locate(registries, IGLOOS, MapDecorationTypes.RED_X)).when(chance(0.25F)));
			} else if (key.equals(PILLAGER_LOOT)) {
				table.withPool(mapPool(ModItems.PILLAGER_OUTPOST_MAP, locate(registries, PILLAGER_OUTPOSTS, MapDecorationTypes.RED_X))
					.when(patrolCaptain()));
			} else if (key.equals(BuiltInLootTables.FISHING_TREASURE)) {
				var biomes = registries.lookupOrThrow(Registries.BIOME);
				table.apply(replacement(Items.JUNGLE_PYRAMID_MAP,
					locate(registries, StructureTags.ON_JUNGLE_PYRAMID_MAPS, MapDecorationTypes.JUNGLE_TEMPLE),
					LocationCheck.checkLocation(LocationPredicate.Builder.location().setBiomes(biomes.getOrThrow(BiomeTags.IS_JUNGLE)))
						.and(chance(FISHING_MAP_CHANCE))));
				table.apply(replacement(Items.SWAMP_HUT_MAP,
					locate(registries, StructureTags.ON_SWAMP_HUT_MAPS, MapDecorationTypes.SWAMP_HUT),
					LocationCheck.checkLocation(LocationPredicate.Builder.location().setBiomes(biomes.getOrThrow(BiomeTags.HAS_SWAMP_HUT)))
						.and(chance(FISHING_MAP_CHANCE))));
			} else if (key.equals(BuiltInLootTables.DESERT_WELL_ARCHAEOLOGY)) {
				var book = ItemPredicate.Builder.item()
					.of(registries.lookupOrThrow(Registries.ITEM), Items.ENCHANTED_BOOK).build();
				var desertMap = replacement(Items.DESERT_PYRAMID_MAP,
					locate(registries, StructureTags.ON_DESERT_PYRAMID_MAPS, MapDecorationTypes.DESERT_PYRAMID), chance(0.20F));
				table.apply(Holder.direct(FilteredFunction.filtered(book).onFail(desertMap.value()).build()));
			} else if (key.equals(BuiltInLootTables.TRIAL_CHAMBERS_SUPPLY)) {
				table.withPool(mapPool(Items.BURIED_ANCIENT_CITY_MAP,
					locate(registries, StructureTags.ON_ANCIENT_CITY_MAPS, MapDecorationTypes.ANCIENT_CITY)).when(chance(0.25F)));
			}
		});
	}

	private static LootPool.Builder mapPool(Item mapItem, Optional<Holder<LootItemFunction>> locator) {
		return LootPool.lootPool().add(LootItem.lootTableItem(mapItem)
			.apply(() -> new StructureMapLootFunction(Optional.empty(), mapItem.builtInRegistryHolder(), locator, false)));
	}

	private static Holder<LootItemFunction> replacement(Item mapItem, Optional<Holder<LootItemFunction>> locator,
		LootItemCondition.Builder condition) {
		return Holder.direct(new StructureMapLootFunction(Optional.of(Holder.direct(condition.build())),
			mapItem.builtInRegistryHolder(), locator, true));
	}

	private static Optional<Holder<LootItemFunction>> locate(HolderLookup.Provider registries, TagKey<Structure> target,
		Holder<MapDecorationType> decoration) {
		return Optional.of(Holder.direct(ExplorationMapFunction.makeExplorationMap(registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(target))
			.setMapDecoration(decoration).setSkipKnownStructures(false).build()));
	}

	private static LootItemCondition.Builder chance(float probability) {
		return LootItemRandomChanceCondition.randomChance(probability);
	}

	private static LootItemCondition.Builder patrolCaptain() {
		ListTag tags = new ListTag();
		tags.add(StringTag.valueOf(PATROL_CAPTAIN_TAG));
		CompoundTag nbt = new CompoundTag();
		nbt.put("Tags", tags);
		return LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
			EntityPredicate.Builder.entity().nbt(new NbtPredicate(nbt)));
	}

	private static ResourceKey<LootTable> wildwaysLoot(String path) {
		return ResourceKey.create(Registries.LOOT_TABLE, Wildways.id(path));
	}

	private static TagKey<Structure> mapTarget(String name) {
		return TagKey.create(Registries.STRUCTURE, Wildways.id("on_" + name + "_maps"));
	}
}
