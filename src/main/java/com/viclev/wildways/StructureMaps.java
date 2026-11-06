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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import net.minecraft.world.level.storage.loot.functions.FilteredFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

public final class StructureMaps {
	public static final String PATROL_CAPTAIN_TAG = "wildways:patrol_captain";
	private static final ResourceKey<LootTable> PILLAGER_LOOT = ResourceKey.create(Registries.LOOT_TABLE,
		Identifier.withDefaultNamespace("entities/pillager"));
	private static final ResourceKey<LootTable> QUARANTINE_ANCHOR = wildwaysLoot("chests/quarantine_grounds/anchor");
	private static final Set<ResourceKey<LootTable>> DUNGEON_CHESTS = Set.of(BuiltInLootTables.SIMPLE_DUNGEON,
		wildwaysLoot("chests/dungeon_zombie"), wildwaysLoot("chests/dungeon_skeleton"), wildwaysLoot("chests/dungeon_spider"));
	private static final MapTarget MINESHAFT = new MapTarget(Items.BURIED_MINESHAFT_MAP, StructureTags.ON_MINESHAFT_MAPS, MapDecorationTypes.MINESHAFT);
	private static final MapTarget DUNGEON = new MapTarget(ModItems.DUNGEON_MAP, null, MapDecorationTypes.RED_X);
	private static final MapTarget TRAIL_RUINS = new MapTarget(ModItems.TRAIL_RUINS_MAP, mapTarget("trail_ruins"), MapDecorationTypes.RED_X);
	private static final MapTarget IGLOO = new MapTarget(ModItems.IGLOO_MAP, mapTarget("igloo"), MapDecorationTypes.RED_X);
	private static final MapTarget PILLAGER_OUTPOST = new MapTarget(ModItems.PILLAGER_OUTPOST_MAP, mapTarget("pillager_outpost"), MapDecorationTypes.RED_X);
	private static final MapTarget JUNGLE_PYRAMID = new MapTarget(Items.JUNGLE_PYRAMID_MAP, StructureTags.ON_JUNGLE_PYRAMID_MAPS, MapDecorationTypes.JUNGLE_TEMPLE);
	private static final MapTarget DESERT_PYRAMID = new MapTarget(Items.DESERT_PYRAMID_MAP, StructureTags.ON_DESERT_PYRAMID_MAPS, MapDecorationTypes.DESERT_PYRAMID);
	private static final MapTarget SWAMP_HUT = new MapTarget(Items.SWAMP_HUT_MAP, StructureTags.ON_SWAMP_HUT_MAPS, MapDecorationTypes.SWAMP_HUT);
	private static final MapTarget ANCIENT_CITY = new MapTarget(Items.BURIED_ANCIENT_CITY_MAP, StructureTags.ON_ANCIENT_CITY_MAPS, MapDecorationTypes.ANCIENT_CITY);
	private static final MapTarget[] TRADE_MAPS = {
		MINESHAFT, DUNGEON, TRAIL_RUINS, IGLOO, PILLAGER_OUTPOST, JUNGLE_PYRAMID, DESERT_PYRAMID, SWAMP_HUT, ANCIENT_CITY
	};
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
				table.withPool(mapPool(registries, MINESHAFT).when(chance(0.25F)));
			} else if (key.equals(BuiltInLootTables.ABANDONED_MINESHAFT)
				|| key.equals(BuiltInLootTables.ABANDONED_CAMP_COMMON_CHEST)
				|| key.equals(BuiltInLootTables.ABANDONED_CAMP_SECRET_CHEST)) {
				table.withPool(mapPool(registries, DUNGEON).when(chance(0.25F)));
			} else if (key.equals(QUARANTINE_ANCHOR)) {
				table.withPool(mapPool(registries, TRAIL_RUINS));
			} else if (key.equals(BuiltInLootTables.VILLAGE_SNOWY_HOUSE)) {
				table.withPool(mapPool(registries, IGLOO).when(chance(0.25F)));
			} else if (key.equals(PILLAGER_LOOT)) {
				table.withPool(mapPool(registries, PILLAGER_OUTPOST).when(patrolCaptain()));
			} else if (key.equals(BuiltInLootTables.FISHING_TREASURE)) {
				var biomes = registries.lookupOrThrow(Registries.BIOME);
				table.apply(replacement(registries, JUNGLE_PYRAMID,
					LocationCheck.checkLocation(LocationPredicate.Builder.location().setBiomes(biomes.getOrThrow(BiomeTags.IS_JUNGLE)))
						.and(chance(FISHING_MAP_CHANCE))));
				table.apply(replacement(registries, SWAMP_HUT,
					LocationCheck.checkLocation(LocationPredicate.Builder.location().setBiomes(biomes.getOrThrow(BiomeTags.HAS_SWAMP_HUT)))
						.and(chance(FISHING_MAP_CHANCE))));
			} else if (key.equals(BuiltInLootTables.DESERT_WELL_ARCHAEOLOGY)) {
				var book = ItemPredicate.Builder.item()
					.of(registries.lookupOrThrow(Registries.ITEM), Items.ENCHANTED_BOOK).build();
				var desertMap = replacement(registries, DESERT_PYRAMID, chance(0.20F));
				table.apply(Holder.direct(FilteredFunction.filtered(book).onFail(desertMap.value()).build()));
			} else if (key.equals(BuiltInLootTables.TRIAL_CHAMBERS_SUPPLY)) {
				table.withPool(mapPool(registries, ANCIENT_CITY).when(chance(0.25F)));
			}
		});
	}

	static ItemStack createTradeMap(ServerLevel level, Vec3 origin, RandomSource random) {
		LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, origin).create(LootContextParamSets.CHEST);
		LootContext context = new LootContext.Builder(params).create(Optional.empty());
		for (MapTarget target : Util.shuffledCopy(TRADE_MAPS, random)) {
			ItemStack map = target.function(level.registryAccess(), Optional.empty(), false).run(ItemStack.EMPTY, context);
			if (!map.isEmpty()) {
				return map;
			}
		}
		return ItemStack.EMPTY;
	}

	static boolean isTradeMap(ItemStack stack) {
		for (MapTarget target : TRADE_MAPS) {
			if (stack.is(target.item())) {
				return true;
			}
		}
		return false;
	}

	private static LootPool.Builder mapPool(HolderLookup.Provider registries, MapTarget target) {
		return LootPool.lootPool().add(LootItem.lootTableItem(target.item())
			.apply(() -> target.function(registries, Optional.empty(), false)));
	}

	private static Holder<LootItemFunction> replacement(HolderLookup.Provider registries, MapTarget target,
		LootItemCondition.Builder condition) {
		return Holder.direct(target.function(registries, Optional.of(Holder.direct(condition.build())), true));
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

	private record MapTarget(Item item, @Nullable TagKey<Structure> structure, Holder<MapDecorationType> decoration) {
		StructureMapLootFunction function(HolderLookup.Provider registries, Optional<Holder<LootItemCondition>> condition, boolean keepOriginal) {
			var locator = this.structure == null ? Optional.<Holder<LootItemFunction>>empty() : locate(registries, this.structure, this.decoration);
			return new StructureMapLootFunction(condition, this.item.builtInRegistryHolder(), locator, keepOriginal);
		}
	}
}
