package com.viclev.wildways;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;

/** Per-world, per-player flags for first-time XP rewards. */
public final class XpProgressionData extends SavedData {
	private record PlayerAwards(String player, List<String> awards) {
		private static final Codec<PlayerAwards> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("player").forGetter(PlayerAwards::player),
			Codec.STRING.listOf().fieldOf("awards").forGetter(PlayerAwards::awards)
		).apply(instance, PlayerAwards::new));
	}

	private static final Codec<XpProgressionData> CODEC = PlayerAwards.CODEC.listOf().xmap(
		XpProgressionData::new,
		XpProgressionData::entries
	);
	public static final SavedDataType<XpProgressionData> TYPE = new SavedDataType<>(
		Wildways.id("xp_progression"),
		XpProgressionData::new,
		CODEC,
		DataFixTypes.SAVED_DATA_COMMAND_STORAGE
	);

	private final Map<UUID, Set<String>> awards = new HashMap<>();

	public XpProgressionData() {
	}

	private XpProgressionData(List<PlayerAwards> entries) {
		for (PlayerAwards entry : entries) {
			try {
				this.awards.put(UUID.fromString(entry.player()), new HashSet<>(entry.awards()));
			} catch (IllegalArgumentException ignored) {
				// Ignore malformed player IDs rather than preventing the world from loading.
			}
		}
	}

	private List<PlayerAwards> entries() {
		return this.awards.entrySet().stream()
			.map(entry -> new PlayerAwards(entry.getKey().toString(), List.copyOf(entry.getValue())))
			.toList();
	}

	public boolean claim(UUID player, String award) {
		boolean claimed = this.awards.computeIfAbsent(player, ignored -> new HashSet<>()).add(award);
		if (claimed) {
			this.setDirty();
		}
		return claimed;
	}
}
