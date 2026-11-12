package com.viclev.wildways;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

public final class BrewingQuests {
	private static final TagKey<Potion> FATAL_POTIONS = TagKey.create(Registries.POTION, Wildways.id("fatal_potions"));
	private static final Identifier USE_FATAL = Wildways.id("quests/use_fatal");
	private static final Identifier TIP_EIGHT_ARROWS = Wildways.id("quests/tip_eight_arrows");
	private static final Identifier TIP_STACK_ARROWS = Wildways.id("quests/tip_stack_arrows");

	private BrewingQuests() {
	}

	public static void applyPotion(Entity owner, LivingEntity target, PotionContents contents) {
		if (owner instanceof ServerPlayer player && target instanceof Mob && contents.is(FATAL_POTIONS)) {
			QuestBookItem.awardQuest(player, USE_FATAL);
		}
	}

	static void fletched(ServerPlayer player, ItemStack result, ItemStack ingredient) {
		if (!result.is(Items.TIPPED_ARROW)) {
			return;
		}
		if (result.getCount() == 8) {
			QuestBookItem.awardQuest(player, TIP_EIGHT_ARROWS);
		} else if (result.getCount() == 64 && ingredient.is(Items.LINGERING_POTION)) {
			QuestBookItem.awardQuest(player, TIP_STACK_ARROWS);
		}
	}
}
