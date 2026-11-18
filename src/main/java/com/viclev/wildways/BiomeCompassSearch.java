package com.viclev.wildways;

import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import org.jspecify.annotations.Nullable;

final class BiomeCompassSearch {
	private static final int HORIZONTAL_STEP = 32;
	private static final int VERTICAL_STEP = 64;
	private static final int SEARCH_RADIUS = 6_400;
	private static final int SAMPLES_PER_SLICE = 1_024;
	private static final int SLICES_PER_TICK = 4;
	private static final long TIME_PER_TICK = TimeUnit.MILLISECONDS.toNanos(2);
	private static final Map<MinecraftServer, ArrayDeque<Request>> QUEUES = new IdentityHashMap<>();

	private final ResourceKey<Biome> biome;
	private final BlockPos origin;
	private final BiomeResolver resolver;
	private final Iterator<MutableBlockPos> columns;
	private final int[] heights;
	private @Nullable MutableBlockPos column;
	private @Nullable BlockPos result;
	private int heightIndex;

	BiomeCompassSearch(ServerLevel level, BlockPos origin, ResourceKey<Biome> biome) {
		var chunks = level.getChunkSource();
		this.biome = biome;
		this.origin = origin;
		this.resolver = chunks.getGenerator().getBiomeSource().createCachingResolver(chunks.randomState());
		// Keep vanilla /locate biome's column order and vertical sampling.
		this.columns = BlockPos.spiralAround(BlockPos.ZERO, SEARCH_RADIUS / HORIZONTAL_STEP, Direction.EAST, Direction.SOUTH).iterator();
		this.heights = Mth.outFromOrigin(origin.getY(), level.getMinY() + 1, level.getMaxY() + 1, VERTICAL_STEP).toArray();
	}

	static void initialize() {
		ServerTickEvents.END_SERVER_TICK.register(BiomeCompassSearch::tick);
		ServerLifecycleEvents.SERVER_STOPPED.register(QUEUES::remove);
	}

	static void enqueue(ServerPlayer player, BiomeCompassMenu menu) {
		QUEUES.computeIfAbsent(player.level().getServer(), ignored -> new ArrayDeque<>()).addLast(new Request(player, menu));
	}

	static void cancel(ServerLevel level, BiomeCompassMenu menu) {
		var queue = QUEUES.get(level.getServer());
		if (queue != null) {
			queue.removeIf(request -> request.menu() == menu);
		}
	}

	private static void tick(MinecraftServer server) {
		ArrayDeque<Request> queue = QUEUES.get(server);
		if (queue == null) {
			return;
		}
		long deadline = System.nanoTime() + TIME_PER_TICK;
		for (int slice = 0; slice < SLICES_PER_TICK && !queue.isEmpty(); slice++) {
			Request request = queue.removeFirst();
			if (request.menu().advanceSearch(request.player(), deadline)) {
				queue.addLast(request);
			}
			if (System.nanoTime() - deadline >= 0) {
				break;
			}
		}
		if (queue.isEmpty()) {
			QUEUES.remove(server);
		}
	}

	boolean advance(long deadline) {
		if (this.heights.length == 0) {
			return true;
		}
		for (int sample = 0; sample < SAMPLES_PER_SLICE; sample++) {
			if (this.column == null) {
				if (!this.columns.hasNext()) {
					return true;
				}
				this.column = this.columns.next();
			}
			int x = this.origin.getX() + this.column.getX() * HORIZONTAL_STEP;
			int z = this.origin.getZ() + this.column.getZ() * HORIZONTAL_STEP;
			int y = this.heights[this.heightIndex++];
			if (this.resolver.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z)).is(this.biome)) {
				this.result = new BlockPos(x, y, z);
				return true;
			}
			if (this.heightIndex == this.heights.length) {
				this.column = null;
				this.heightIndex = 0;
			}
			if ((sample + 1) % 64 == 0 && System.nanoTime() - deadline >= 0) {
				break;
			}
		}
		return false;
	}

	@Nullable BlockPos result() {
		return this.result;
	}

	private record Request(ServerPlayer player, BiomeCompassMenu menu) {
	}
}
