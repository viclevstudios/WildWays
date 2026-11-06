package com.viclev.wildways;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class DungeonLocations extends SavedData {
	private static final int SEARCH_RADIUS = 1_024;
	private static final SavedDataType<DungeonLocations> TYPE = new SavedDataType<>(
		Wildways.id("dungeon_locations"),
		() -> new DungeonLocations(List.of()),
		BlockPos.CODEC.listOf().fieldOf("rooms").codec().xmap(DungeonLocations::new, data -> List.copyOf(data.rooms)),
		DataFixTypes.SAVED_DATA_COMMAND_STORAGE
	);

	private final Set<BlockPos> rooms;

	private DungeonLocations(List<BlockPos> rooms) {
		this.rooms = new HashSet<>(rooms);
	}

	static void initialize() {
		ServerChunkEvents.CHUNK_LOAD.register((level, chunk, newlyGenerated) -> indexChunk(level, chunk));
	}

	static void indexChunk(ServerLevel level, LevelChunk chunk) {
		for (BlockPos pos : chunk.getBlockEntitiesPos()) {
			if (!chunk.getBlockState(pos).is(Blocks.SPAWNER) || !hasDungeonFloor(chunk, pos.below())
				|| !(chunk.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner)) {
				continue;
			}
			String mob = spawner.getUpdateTag(level.registryAccess()).getCompoundOrEmpty("SpawnData")
				.getCompoundOrEmpty("entity").getStringOr("id", "");
			if (mob.equals("minecraft:zombie") || mob.equals("minecraft:skeleton") || mob.equals("minecraft:spider")) {
				DungeonLocations data = level.getDataStorage().computeIfAbsent(TYPE);
				if (data.rooms.add(pos.immutable())) {
					data.setDirty();
				}
			}
		}
	}

	// Vanilla monster rooms are features. The spawner and its cobblestone floor identify
	// a room in existing saves as well as in new terrain, without loading adjacent chunks.
	private static boolean hasDungeonFloor(LevelChunk chunk, BlockPos floor) {
		if (!isDungeonFloor(chunk, floor)) {
			return false;
		}
		int matchingNeighbors = 0;
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos neighbor = floor.relative(direction);
			if (ChunkPos.containing(neighbor).equals(chunk.getPos()) && isDungeonFloor(chunk, neighbor)) {
				matchingNeighbors++;
			}
		}
		return matchingNeighbors >= 2;
	}

	private static boolean isDungeonFloor(LevelChunk chunk, BlockPos pos) {
		var state = chunk.getBlockState(pos);
		return state.is(Blocks.MOSSY_COBBLESTONE) || state.is(Blocks.COBBLESTONE);
	}

	static @Nullable BlockPos findNearest(ServerLevel level, BlockPos origin) {
		DungeonLocations data = level.getDataStorage().get(TYPE);
		if (data == null) {
			return null;
		}
		BlockPos nearest = null;
		double nearestDistance = SEARCH_RADIUS * SEARCH_RADIUS;
		for (BlockPos pos : data.rooms) {
			double distance = pos.distSqr(origin);
			if (distance < nearestDistance) {
				nearest = pos;
				nearestDistance = distance;
			}
		}
		return nearest;
	}
}
