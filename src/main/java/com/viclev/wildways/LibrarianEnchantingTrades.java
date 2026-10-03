package com.viclev.wildways;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

/** Book donations unlock a persistent, ordinary merchant offer on that villager. */
public final class LibrarianEnchantingTrades {
	private LibrarianEnchantingTrades() {
	}

	public static void initialize() {
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			if (!(entity instanceof Villager villager) || !villager.getVillagerData().profession().is(VillagerProfession.LIBRARIAN)
				|| player.isSpectator() || villager.isBaby() || !villager.isAlive()) {
				return InteractionResult.PASS;
			}
			ItemStack book = player.getItemInHand(hand);
			boolean donation = book.is(Items.ENCHANTED_BOOK) && villager.getVillagerData().level() >= 5;
			if (level instanceof ServerLevel) {
				MerchantOffers offers = villager.getOffers();
				// Also migrate book sales on existing villagers before their trading screen opens.
				offers.removeIf(offer -> offer.getResult().is(Items.ENCHANTED_BOOK) || offer.getResult().is(Items.BOOKSHELF));
				ensureOffer(offers, Items.CHISELED_BOOKSHELF, 5, 1);
				if (villager.getVillagerData().level() >= 2) {
					ensureOffer(offers, EnchantingItems.RUNES.get(0), 10, 5);
					ensureOffer(offers, EnchantingItems.RUNES.get(1), 20, 5);
				}
				if (villager.getVillagerData().level() >= 3) {
					ensureOffer(offers, ModItems.BIOME_COMPASS, 5, 10);
				}
				if (donation) {
					if (villager.isTrading()) {
						return InteractionResult.SUCCESS;
					}
					if (EnchantingRules.recipes(book).isEmpty()) {
						return InteractionResult.PASS;
					}
					Item catalyst = EnchantingItems.CATALYST;
					if (hasOffer(offers, catalyst)) {
						player.sendOverlayMessage(Component.translatable("message.wildways.catalyst_already_unlocked"));
					} else {
						offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 18), new ItemStack(catalyst), 12, 30, 0.05F));
						book.consume(1, player);
						player.sendOverlayMessage(Component.translatable("message.wildways.catalyst_unlocked"));
					}
				}
			}
			return donation ? InteractionResult.SUCCESS : InteractionResult.PASS;
		});
	}

	private static boolean hasOffer(MerchantOffers offers, Item item) {
		return offers.stream().anyMatch(offer -> offer.getResult().is(item));
	}

	private static void ensureOffer(MerchantOffers offers, Item item, int price, int xp) {
		if (!hasOffer(offers, item)) {
			offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, price), new ItemStack(item), 12, xp, 0.05F));
		}
	}
}
