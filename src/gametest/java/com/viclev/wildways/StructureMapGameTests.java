package com.viclev.wildways;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class StructureMapGameTests {
	@GameTest
	public void indexedDungeonSupportsLootAndWanderingTraderMaps(GameTestHelper helper) {
		var level = helper.getLevel();
		BlockPos spawnerPos = helper.absolutePos(new BlockPos(2, 2, 2));
		level.setBlockAndUpdate(spawnerPos.below(), Blocks.MOSSY_COBBLESTONE.defaultBlockState());
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			level.setBlockAndUpdate(spawnerPos.below().relative(direction), Blocks.COBBLESTONE.defaultBlockState());
		}
		level.setBlockAndUpdate(spawnerPos, Blocks.SPAWNER.defaultBlockState());
		((SpawnerBlockEntity)level.getBlockEntity(spawnerPos)).setEntityId(EntityTypes.SKELETON, level.getRandom());
		DungeonLocations.indexChunk(level, level.getChunkAt(spawnerPos));
		helper.assertTrue(spawnerPos.equals(DungeonLocations.findNearest(level, spawnerPos)), "The monster room must be indexed");

		var params = new LootParams.Builder(level)
			.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(spawnerPos))
			.create(LootContextParamSets.CHEST);
		for (var key : List.of(BuiltInLootTables.ABANDONED_MINESHAFT, BuiltInLootTables.ABANDONED_CAMP_COMMON_CHEST)) {
			var table = level.getServer().reloadableRegistries().getLootTable(key);
			ItemStack found = ItemStack.EMPTY;
			for (long seed = 1; seed <= 32 && found.isEmpty(); seed++) {
				found = table.getRandomItems(params, seed * 104729).stream()
					.filter(item -> item.is(ModItems.DUNGEON_MAP)).findFirst().orElse(ItemStack.EMPTY);
			}
			helper.assertTrue(found.has(DataComponents.MAP_ID), "Expected a filled dungeon map in " + key.identifier());
			helper.assertTrue(found.is(ItemTags.CLONABLE_MAPS), "The dungeon map must be cloneable");
			var target = found.get(DataComponents.MAP_DECORATIONS).decorations().get("+");
			helper.assertTrue(target != null && target.x() == spawnerPos.getX() && target.z() == spawnerPos.getZ(),
				"The dungeon map must point to the indexed spawner");
			helper.assertTrue(target != null && target.type().value().assetId().equals(Wildways.id("dungeon")),
				"The dungeon map must use the Wildways dungeon marker");
		}

		var trader = helper.spawn(EntityTypes.WANDERING_TRADER, new BlockPos(3, 2, 3));
		var offers = trader.getOffers();
		int before = offers.size();
		RandomSource seeds = RandomSource.create(42L);
		long seed;
		RandomSource probe;
		do {
			seed = seeds.nextLong();
			probe = RandomSource.create(seed);
			EnchantedBookSources.randomBook(level, probe);
		} while (!probe.nextBoolean());
		trader.getRandom().setSeed(seed);

		var player = helper.makeMockPlayer(GameType.SURVIVAL);
		UseEntityCallback.EVENT.invoker().interact(player, level, InteractionHand.MAIN_HAND, trader, null);
		helper.assertTrue(offers.size() == before + 2 && offers.stream().noneMatch(offer -> offer.getResult().is(ModItems.BIOME_COMPASS)),
			"The map branch must add one exploration offer alongside the book");
		var mapOffer = offers.getLast();
		int price = mapOffer.getBaseCostA().getCount();
		helper.assertTrue(StructureMaps.isTradeMap(mapOffer.getResult()) && mapOffer.getResult().has(DataComponents.MAP_ID)
			&& mapOffer.getBaseCostA().is(Items.EMERALD) && price >= 12 && price <= 20 && mapOffer.getMaxUses() == 1,
			"The trader must sell a filled structure map for twelve to twenty emeralds");
		mapOffer.increaseUses();
		UseEntityCallback.EVENT.invoker().interact(player, level, InteractionHand.MAIN_HAND, trader, null);
		helper.assertTrue(offers.size() == before + 2 && offers.getLast() == mapOffer && mapOffer.isOutOfStock(),
			"The same map, price and stock must persist when the trade menu reopens");
		helper.succeed();
	}

	@GameTest
	public void vanillaPatrolSpawnerMarksItsCaptain(GameTestHelper helper) throws ReflectiveOperationException {
		var level = helper.getLevel();
		var spawner = new PatrolSpawner();
		var spawnMember = PatrolSpawner.class.getDeclaredMethod("spawnPatrolMember",
			ServerLevel.class, BlockPos.class, RandomSource.class, boolean.class);
		spawnMember.setAccessible(true);
		for (boolean captain : List.of(true, false)) {
			BlockPos relativePos = new BlockPos(captain ? 1 : 3, 2, 1);
			helper.setBlock(relativePos.below(), Blocks.STONE);
			helper.setBlock(relativePos, Blocks.AIR);
			helper.setBlock(relativePos.above(), Blocks.AIR);
			helper.assertTrue((boolean)spawnMember.invoke(spawner, level, helper.absolutePos(relativePos), RandomSource.create(42L), captain),
				"Vanilla must spawn the patrol member");
			var pillager = helper.findEntities(EntityTypes.PILLAGER, Vec3.atCenterOf(relativePos), 1).getFirst();
			helper.assertTrue(pillager.isPatrolLeader() == captain
				&& pillager.entityTags().contains(StructureMaps.PATROL_CAPTAIN_TAG) == captain,
				"Vanilla patrol spawning must mark only the captain for the outpost map drop");
		}
		helper.succeed();
	}

	@GameTest
	public void onlyPatrolCaptainsAreMarkedForMapLoot(GameTestHelper helper) {
		var level = helper.getLevel();
		var pos = helper.absolutePos(new BlockPos(1, 1, 1));
		var difficulty = level.getCurrentDifficultyAt(pos);
		var leader = EntityTypes.PILLAGER.create(level, EntitySpawnReason.PATROL);
		leader.setPatrolLeader(true);
		leader.finalizeSpawn(level, difficulty, EntitySpawnReason.PATROL, null);
		helper.assertTrue(leader.entityTags().contains(StructureMaps.PATROL_CAPTAIN_TAG), "Mark the roaming captain");
		var member = EntityTypes.PILLAGER.create(level, EntitySpawnReason.PATROL);
		member.finalizeSpawn(level, difficulty, EntitySpawnReason.PATROL, null);
		helper.assertFalse(member.entityTags().contains(StructureMaps.PATROL_CAPTAIN_TAG), "An ordinary patrol member cannot drop a map");
		var outpostCaptain = EntityTypes.PILLAGER.create(level, EntitySpawnReason.STRUCTURE);
		outpostCaptain.setPatrolLeader(true);
		outpostCaptain.finalizeSpawn(level, difficulty, EntitySpawnReason.STRUCTURE, null);
		helper.assertFalse(outpostCaptain.entityTags().contains(StructureMaps.PATROL_CAPTAIN_TAG), "An outpost captain cannot drop a map");
		helper.succeed();
	}
}
