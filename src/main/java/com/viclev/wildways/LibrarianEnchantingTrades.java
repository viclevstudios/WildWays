package com.viclev.wildways;

import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

/** Updates existing librarians to the current trades when they are opened. */
public final class LibrarianEnchantingTrades {
	private static final ResourceKey<Enchantment> ATTUNEMENT = ResourceKey.create(Registries.ENCHANTMENT, Wildways.id("attunement"));

	private LibrarianEnchantingTrades() {
	}

	public static void initialize() {
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			if (!(entity instanceof Villager villager) || !villager.getVillagerData().profession().is(VillagerProfession.LIBRARIAN)
				|| player.isSpectator() || villager.isBaby() || !villager.isAlive()) {
				return InteractionResult.PASS;
			}
			if (level instanceof ServerLevel serverLevel) {
				MerchantOffers offers = villager.getOffers();
				ItemStack attunementBook = attunementBook(serverLevel);
				// Remove old random book sales and offers replaced by the current trade sets.
				offers.removeIf(offer -> offer.getResult().is(EnchantingItems.CATALYST)
					|| offer.getResult().is(Items.LANTERN) || offer.getResult().is(Items.GLASS)
					|| (offer.getResult().is(Items.ENCHANTED_BOOK)
						&& !ItemStack.isSameItemSameComponents(offer.getResult(), attunementBook)));
				// Earlier versions granted every alternative; keep only the permitted number per level.
				keepAtMost(offers, offer -> isPaperSale(offer) || offer.getResult().is(Items.CHISELED_BOOKSHELF), 1, villager.getRandom());
				keepAtMost(offers, offer -> isRune(offer), 1, villager.getRandom());
				keepAtMost(offers, offer -> isWritableBookSale(offer) || offer.getResult().is(Items.CLOCK)
					|| offer.getResult().is(Items.COMPASS), 2, villager.getRandom());
				keepAtMost(offers, offer -> offer.getResult().is(Items.DYED_CANDLE.red())
					|| offer.getResult().is(Items.DYED_CANDLE.yellow()), 1, villager.getRandom());
				ensureOffer(offers, Items.BOOKSHELF, 9, 1);
				if (offers.stream().noneMatch(offer -> isPaperSale(offer) || offer.getResult().is(Items.CHISELED_BOOKSHELF))) {
					ensureOffer(offers, Items.CHISELED_BOOKSHELF, 5, 1);
				}
				if (villager.getVillagerData().level() >= 2) {
					if (offers.stream().noneMatch(LibrarianEnchantingTrades::isBookSale)) {
						offers.add(new MerchantOffer(new ItemCost(Items.BOOK, 4), new ItemStack(Items.EMERALD), 12, 10, 0.05F));
					}
					if (offers.stream().noneMatch(LibrarianEnchantingTrades::isRune)) {
						int tier = villager.getRandom().nextInt(2);
						ensureOffer(offers, EnchantingItems.RUNES.get(tier), tier == 0 ? 10 : 20, 5);
					}
				}
				if (villager.getVillagerData().level() >= 3) {
					ensureOffer(offers, ModItems.BIOME_COMPASS, 5, 10);
				}
				if (villager.getVillagerData().level() >= 5 && !hasOffer(offers, attunementBook)) {
					offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 18), attunementBook, 12, 30, 0.05F));
				}
			}
			return InteractionResult.PASS;
		});
	}

	private static ItemStack attunementBook(ServerLevel level) {
		ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
		book.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ATTUNEMENT), 1);
		return book;
	}

	private static boolean hasOffer(MerchantOffers offers, Item item) {
		return offers.stream().anyMatch(offer -> offer.getResult().is(item));
	}

	private static boolean hasOffer(MerchantOffers offers, ItemStack result) {
		return offers.stream().anyMatch(offer -> ItemStack.isSameItemSameComponents(offer.getResult(), result));
	}

	private static boolean isPaperSale(MerchantOffer offer) {
		return offer.getResult().is(Items.EMERALD) && offer.getBaseCostA().is(Items.PAPER);
	}

	private static boolean isBookSale(MerchantOffer offer) {
		return offer.getResult().is(Items.EMERALD) && offer.getBaseCostA().is(Items.BOOK);
	}

	private static boolean isWritableBookSale(MerchantOffer offer) {
		return offer.getResult().is(Items.EMERALD) && offer.getBaseCostA().is(Items.WRITABLE_BOOK);
	}

	private static boolean isRune(MerchantOffer offer) {
		return offer.getResult().is(EnchantingItems.RUNES.get(0)) || offer.getResult().is(EnchantingItems.RUNES.get(1));
	}

	private static void keepAtMost(MerchantOffers offers, Predicate<MerchantOffer> predicate, int maximum, RandomSource random) {
		List<MerchantOffer> matches = offers.stream().filter(predicate).toList();
		while (matches.size() > maximum) {
			int lowestUses = matches.stream().mapToInt(MerchantOffer::getUses).min().orElse(0);
			List<MerchantOffer> candidates = matches.stream().filter(offer -> offer.getUses() == lowestUses).toList();
			offers.remove(candidates.get(random.nextInt(candidates.size())));
			matches = offers.stream().filter(predicate).toList();
		}
	}

	private static void ensureOffer(MerchantOffers offers, Item item, int price, int xp) {
		if (!hasOffer(offers, item)) {
			offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, price), new ItemStack(item), 12, xp, 0.05F));
		}
	}
}
