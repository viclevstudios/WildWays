package com.viclev.wildways;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.ShulkerBoxBlock;

final class CollectorQuests {
	private static final Identifier COLLECTION = Wildways.id("quests/collect_portal_eyes");
	private static final Identifier ACTIVATE_PORTAL = Wildways.id("quests/activate_end_portal");
	private static final int REQUIRED_EYES = 12;

	private CollectorQuests() {
	}

	static void checkInventory(ServerPlayer player) {
		var advancements = player.getAdvancements();
		AdvancementHolder quest = player.level().getServer().getAdvancements().get(COLLECTION);
		if (quest == null) {
			return;
		}
		var progress = advancements.getOrStartProgress(quest);
		if (progress.isDone()) {
			return;
		}

		int count = Math.min(REQUIRED_EYES, countEyes(player.getInventory()));
		for (int step = 1; step <= count; step++) {
			String criterion = "eyes_" + step;
			var completed = progress.getCriterion(criterion);
			if (completed != null && !completed.isDone()) {
				advancements.award(quest, criterion);
			}
		}
	}

	private static int countEyes(Inventory inventory) {
		Set<Item> found = new HashSet<>();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.isEmpty()) {
				continue;
			}
			addEye(found, stack);
			if (stack.is(ModBlocks.ENDERMITE_BOX.asItem())
				|| stack.getItem() instanceof BlockItem item && item.getBlock() instanceof ShulkerBoxBlock) {
				var contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
				for (var stored : contents.nonEmptyItems()) {
					addEye(found, stored);
				}
			}
		}
		return found.size();
	}

	private static void addEye(Set<Item> found, ItemInstance stack) {
		Item item = stack.typeHolder().value();
		if (item == ModItems.EYE_OF_VOLCANITE) {
			found.add(ModItems.ATTUNED_EYE_OF_VOLCANITE);
		} else if (ModItems.isPortalEye(item) && (item != ModItems.ENCHANTED_EYE
			|| !stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty())) {
			found.add(item);
		}
	}

	static void portalOpened(ServerPlayer player) {
		AdvancementHolder quest = player.level().getServer().getAdvancements().get(ACTIVATE_PORTAL);
		if (quest != null) {
			player.getAdvancements().award(quest, "opened");
		}
	}
}
