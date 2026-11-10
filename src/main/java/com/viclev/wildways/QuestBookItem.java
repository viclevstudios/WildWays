package com.viclev.wildways;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

final class QuestBookItem extends Item {
	private static final Identifier STARTER_BOOK = Wildways.id("quests/starter_book");

	QuestBookItem(Properties properties) {
		super(properties);
	}

	static void initialize() {
		PayloadTypeRegistry.clientboundPlay().register(QuestGuide.Open.TYPE, QuestGuide.Open.CODEC);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> giveStarterBook(handler.getPlayer()));
	}

	static void giveStarterBook(ServerPlayer player) {
		AdvancementHolder starter = player.level().getServer().getAdvancements().get(STARTER_BOOK);
		if (starter != null) {
			// The advancement's rewards hand out the book and unlock its recipe once per player.
			player.getAdvancements().award(starter, "joined_world");
		}
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS_SERVER;
		}
		if (player instanceof ServerPlayer serverPlayer && ServerPlayNetworking.canSend(serverPlayer, QuestGuide.Open.TYPE)) {
			ServerPlayNetworking.send(serverPlayer, new QuestGuide.Open(readProgress(serverPlayer)));
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.PASS;
	}

	static Map<Identifier, QuestGuide.Progress> readProgress(ServerPlayer player) {
		var advancements = player.level().getServer().getAdvancements();
		Map<Identifier, QuestGuide.Progress> progress = new HashMap<>();
		for (QuestGuide.Group group : QuestGuide.GROUPS) {
			for (QuestGuide.Quest quest : group.quests()) {
				AdvancementHolder advancement = advancements.get(quest.advancement());
				if (advancement != null) {
					AdvancementProgress playerProgress = player.getAdvancements().getOrStartProgress(advancement);
					var requirements = advancement.value().requirements();
					int completed = requirements.count(criterion -> playerProgress.getCriterion(criterion).isDone());
					progress.put(quest.advancement(), new QuestGuide.Progress(completed, requirements.size()));
				}
			}
		}
		return progress;
	}
}
