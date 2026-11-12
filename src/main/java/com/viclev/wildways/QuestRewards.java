package com.viclev.wildways;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.gamerules.GameRules;

public final class QuestRewards {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Settings DEFAULTS = new Settings(25, Map.of());
	private static Settings settings = DEFAULTS;

	private QuestRewards() {
	}

	static void initialize() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> settings = readSettings());
	}

	public static void complete(ServerPlayer player, Identifier advancement) {
		var quest = QuestGuide.questForAdvancement(advancement);
		if (quest == null || !XpProgression.claim(player, "quest:" + advancement)) {
			return;
		}

		int xp = settings.xpFor(quest.id());
		if (xp > 0) {
			player.giveExperiencePoints(xp);
		}
		player.connection.send(new ClientboundSoundPacket(Holder.direct(SoundEvents.PLAYER_LEVELUP),
			SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.65F, 1.0F, player.getRandom().nextLong()));
		Component title = Component.translatable("chat.square_brackets", quest.title()).withStyle(ChatFormatting.GREEN);
		Component announcement = Component.translatable("wildways.guide.quest_completed", player.getDisplayName(), title);
		if (player.level().getGameRules().get(GameRules.SHOW_ADVANCEMENT_MESSAGES)) {
			player.level().getServer().getPlayerList().broadcastSystemMessage(announcement, false);
		} else {
			player.sendSystemMessage(announcement);
		}
	}

	private static Settings readSettings() {
		var path = FabricLoader.getInstance().getConfigDir().resolve("wildways-quests.json");
		try {
			if (Files.notExists(path)) {
				Files.createDirectories(path.getParent());
				Files.writeString(path, GSON.toJson(DEFAULTS) + "\n");
				return DEFAULTS;
			}
			try (var reader = Files.newBufferedReader(path)) {
				Settings loaded = GSON.fromJson(reader, Settings.class);
				return loaded == null ? DEFAULTS : loaded;
			}
		} catch (IOException | JsonParseException exception) {
			Wildways.LOGGER.warn("Could not load {}; using default quest rewards", path, exception);
			return DEFAULTS;
		}
	}

	record Settings(Integer defaultXp, Map<String, Integer> questXp) {
		int xpFor(String id) {
			Integer override = this.questXp == null ? null : this.questXp.get(id);
			if (override != null && override >= 0) {
				return override;
			}
			return this.defaultXp != null && this.defaultXp >= 0 ? this.defaultXp : DEFAULTS.defaultXp;
		}
	}
}
