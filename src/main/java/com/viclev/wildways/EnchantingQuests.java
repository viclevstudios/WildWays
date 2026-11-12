package com.viclev.wildways;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class EnchantingQuests {
	private static final Identifier ENCHANT_LEVEL_FIVE = Wildways.id("quests/enchant_level_five");
	private static final Identifier USE_CATALYST = Wildways.id("quests/use_enchanting_catalyst");
	private static final Identifier REPAIR_MENDING_ITEM = Wildways.id("quests/repair_mending_item");

	private EnchantingQuests() {
	}

	static void enchanted(ServerPlayer player, int enchantmentLevel, boolean discounted) {
		if (enchantmentLevel == 5) {
			QuestBookItem.awardQuest(player, ENCHANT_LEVEL_FIVE);
		}
		if (discounted && !player.hasInfiniteMaterials()) {
			QuestBookItem.awardQuest(player, USE_CATALYST);
		}
	}

	public static void repaired(ServerPlayer player) {
		QuestBookItem.awardQuest(player, REPAIR_MENDING_ITEM);
	}
}
