package com.viclev.wildways;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
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
	public void campAndMineshaftMapsPointToTheIndexedSpawner(GameTestHelper helper) {
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
