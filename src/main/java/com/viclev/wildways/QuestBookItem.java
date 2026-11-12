package com.viclev.wildways;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

final class QuestBookItem extends Item {
	private static final Identifier STARTER_BOOK = Wildways.id("quests/starter_book");
	private static final Identifier VISIT_QUARANTINE = Wildways.id("quests/quarantine_grounds");
	private static final ResourceKey<Structure> QUARANTINE_GROUNDS = ResourceKey.create(Registries.STRUCTURE,
		Wildways.id("quarantine_grounds"));

	QuestBookItem(Properties properties) {
		super(properties);
	}

	static void initialize() {
		EquipmentQuests.initialize();
		AnimalWeightsQuests.initialize();
		PayloadTypeRegistry.clientboundPlay().register(QuestGuide.Open.TYPE, QuestGuide.Open.CODEC);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> giveStarterBook(handler.getPlayer()));
		ServerTickEvents.END_LEVEL_TICK.register(level -> {
			if (level.getGameTime() % 20 == 0 && !level.players().isEmpty()) {
				checkQuarantineGrounds(level);
				level.players().forEach(CollectorQuests::checkInventory);
			}
		});
	}

	static void awardQuest(ServerPlayer player, Identifier id) {
		AdvancementHolder quest = player.level().getServer().getAdvancements().get(id);
		if (quest != null) {
			player.getAdvancements().award(quest, "complete");
		}
	}

	private static void checkQuarantineGrounds(ServerLevel level) {
		AdvancementHolder quest = level.getServer().getAdvancements().get(VISIT_QUARANTINE);
		var structure = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).get(QUARANTINE_GROUNDS);
		if (quest == null || structure.isEmpty()) {
			return;
		}
		for (ServerPlayer player : level.players()) {
			var pos = player.blockPosition();
			// Use the full structure box, including the spaces between jigsaw pieces.
			if (!player.getAdvancements().getOrStartProgress(quest).isDone() && level.hasChunkAt(pos)
				&& level.structureManager().getStructureAt(pos, structure.get().value()).isValid()) {
				player.getAdvancements().award(quest, "visit");
			}
		}
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
		CollectorQuests.checkInventory(player);
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
