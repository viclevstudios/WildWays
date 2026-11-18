package com.viclev.wildways;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jspecify.annotations.Nullable;

/** Holds natural terrain heights until the last chunk of a structure piece is placed. */
public final class QuarantinePiecePlacementContext {
	private static final ThreadLocal<QuarantinePiecePlacementContext> ACTIVE = new ThreadLocal<>();
	private final LongSet pendingChunks;
	private final Long2IntMap heights = new Long2IntOpenHashMap();

	public QuarantinePiecePlacementContext(BoundingBox bounds) {
		this(new LongOpenHashSet(bounds.intersectingChunks().mapToLong(ChunkPos::pack).toArray()));
	}

	private QuarantinePiecePlacementContext(LongSet pendingChunks) {
		this.pendingChunks = pendingChunks;
	}

	public void place(Runnable action) {
		var previous = ACTIVE.get();
		ACTIVE.set(this);
		try {
			action.run();
		} finally {
			if (previous == null) {
				ACTIVE.remove();
			} else {
				ACTIVE.set(previous);
			}
		}
	}

	public boolean finishChunk(ChunkPos chunk) {
		this.pendingChunks.remove(chunk.pack());
		return this.pendingChunks.isEmpty();
	}

	static Long2IntMap groundHeights() {
		var active = ACTIVE.get();
		return active == null ? new Long2IntOpenHashMap() : active.heights;
	}

	static boolean isActive() {
		return ACTIVE.get() != null;
	}

	public CompoundTag save() {
		var tag = new CompoundTag();
		long[] columns = new long[this.heights.size()];
		int[] heights = new int[columns.length];
		int index = 0;
		for (var entry : this.heights.long2IntEntrySet()) {
			columns[index] = entry.getLongKey();
			heights[index++] = entry.getIntValue();
		}
		tag.putLongArray("pending_chunks", this.pendingChunks.toLongArray());
		tag.putLongArray("columns", columns);
		tag.putIntArray("heights", heights);
		return tag;
	}

	public static @Nullable QuarantinePiecePlacementContext load(CompoundTag tag) {
		long[] pending = tag.getLongArray("pending_chunks").orElse(null);
		long[] columns = tag.getLongArray("columns").orElse(null);
		int[] heights = tag.getIntArray("heights").orElse(null);
		if (pending == null || pending.length == 0 || columns == null || heights == null || columns.length != heights.length) {
			return null;
		}
		var placement = new QuarantinePiecePlacementContext(new LongOpenHashSet(pending));
		for (int index = 0; index < columns.length; index++) {
			placement.heights.put(columns[index], heights[index]);
		}
		return placement;
	}
}
