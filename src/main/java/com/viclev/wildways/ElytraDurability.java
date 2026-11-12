package com.viclev.wildways;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ElytraDurability {
	private ElytraDurability() {
	}

	public static boolean isAtLastPoint(ItemStack stack) {
		return stack.has(DataComponents.GLIDER) && stack.isDamageableItem()
			&& stack.getMaxDamage() - stack.getDamageValue() <= 1;
	}

	public static void onWornGliderLanding(ServerPlayer player, ItemStack glider) {
		if (player.isAlive() && glider.is(Items.ELYTRA)) {
			QuestBookItem.awardQuest(player, Wildways.id("quests/land_with_worn_elytra"));
		}
	}
}
