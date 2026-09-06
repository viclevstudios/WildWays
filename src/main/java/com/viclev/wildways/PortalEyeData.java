package com.viclev.wildways;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	private final Map<Long, ItemStack> eyes = new HashMap<>();

	public PortalEyeData() {
	}

	private PortalEyeData(List<PlacedEye> entries) {
		entries.forEach(entry -> this.eyes.put(entry.pos(), entry.stack().copyWithCount(1)));
	}

	private List<PlacedEye> entries() {
		return this.eyes.entrySet().stream()
			.map(entry -> new PlacedEye(entry.getKey(), entry.getValue()))
			.toList();
	}

	public ItemStack get(BlockPos pos) {
		ItemStack stack = this.eyes.get(pos.asLong());
		return stack == null ? ItemStack.EMPTY : stack.copy();
	}

	public void put(BlockPos pos, ItemStack stack) {
		this.eyes.put(pos.asLong(), stack.copyWithCount(1));
		this.setDirty();
	}

	public ItemStack remove(BlockPos pos) {
		ItemStack stack = this.eyes.remove(pos.asLong());
		if (stack != null) {
			this.setDirty();
			return stack;
		}
		return ItemStack.EMPTY;
	}
}
