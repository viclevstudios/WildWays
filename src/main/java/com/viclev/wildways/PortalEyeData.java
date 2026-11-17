package com.viclev.wildways;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public final class PortalEyeData extends SavedData {
	private record PlacedEye(long pos, ItemStack stack) {
		private static final Codec<PlacedEye> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.LONG.fieldOf("pos").forGetter(PlacedEye::pos),
			ItemStack.CODEC.fieldOf("stack").forGetter(PlacedEye::stack)
		).apply(instance, PlacedEye::new));
	}

	private static final Codec<PortalEyeData> CODEC = PlacedEye.CODEC.listOf().xmap(
		PortalEyeData::new,
		PortalEyeData::entries
	);
	public static final SavedDataType<PortalEyeData> TYPE = new SavedDataType<>(
		Wildways.id("portal_eyes"),
		PortalEyeData::new,
		CODEC,
		DataFixTypes.SAVED_DATA_COMMAND_STORAGE
	);

	private final Long2ObjectMap<Long2ObjectMap<ItemStack>> eyesByChunk = new Long2ObjectOpenHashMap<>();

	public PortalEyeData() {
	}

	private PortalEyeData(List<PlacedEye> entries) {
		for (PlacedEye entry : entries) {
			this.store(BlockPos.of(entry.pos()), entry.stack());
		}
	}

	private List<PlacedEye> entries() {
		List<PlacedEye> entries = new ArrayList<>();
		for (var eyes : this.eyesByChunk.values()) {
			for (var entry : eyes.long2ObjectEntrySet()) {
				entries.add(new PlacedEye(entry.getLongKey(), entry.getValue()));
			}
		}
		return entries;
	}

	public ItemStack get(BlockPos pos) {
		var eyes = this.eyesByChunk.get(ChunkPos.pack(pos));
		ItemStack stack = eyes == null ? null : eyes.get(pos.asLong());
		return stack == null ? ItemStack.EMPTY : stack.copy();
	}

	public void forEachInChunk(ChunkPos chunk, BiConsumer<BlockPos, ItemStack> consumer) {
		var eyes = this.eyesByChunk.get(chunk.pack());
		if (eyes != null) {
			for (var entry : eyes.long2ObjectEntrySet()) {
				consumer.accept(BlockPos.of(entry.getLongKey()), entry.getValue());
			}
		}
	}

	public void put(BlockPos pos, ItemStack stack) {
		this.store(pos, stack);
		this.setDirty();
	}

	private void store(BlockPos pos, ItemStack stack) {
		this.eyesByChunk.computeIfAbsent(ChunkPos.pack(pos), key -> new Long2ObjectOpenHashMap<>())
			.put(pos.asLong(), stack.copyWithCount(1));
	}

	public ItemStack remove(BlockPos pos) {
		long chunk = ChunkPos.pack(pos);
		var eyes = this.eyesByChunk.get(chunk);
		ItemStack stack = eyes == null ? null : eyes.remove(pos.asLong());
		if (stack == null) {
			return ItemStack.EMPTY;
		}
		if (eyes.isEmpty()) {
			this.eyesByChunk.remove(chunk);
		}
		this.setDirty();
		return stack;
	}
}
