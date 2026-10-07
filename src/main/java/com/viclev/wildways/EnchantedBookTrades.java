package com.viclev.wildways;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

/** Replaces one master trade with the planned book and adds a wanderer's find. */
public final class EnchantedBookTrades {
	private static final String MASTER_MARKER = "wildways:master_book_offer";
	private static final String TRADER_MARKER = "wildways:wandering_book_offer";

	private EnchantedBookTrades() {
	}

	public static void initialize() {
		UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
			if (!(world instanceof ServerLevel level) || player.isSpectator() || !entity.isAlive()) {
				return InteractionResult.PASS;
			}
			if (entity instanceof WanderingTrader trader && !trader.entityTags().contains(TRADER_MARKER)) {
				trader.getOffers().add(offer(EnchantedBookSources.randomBook(level, trader.getRandom()), 20));
				trader.addTag(TRADER_MARKER);
			} else if (entity instanceof Villager villager && !villager.isBaby()
				&& villager.getVillagerData().level() >= 5 && !villager.entityTags().contains(MASTER_MARKER)) {
				ResourceKey<Enchantment> enchantment = masterBook(villager);
				if (enchantment != null) {
					MerchantOffers offers = villager.getOffers();
					// Vanilla picks up to two offers at each level. Replace one master
					// offer so the new book never creates a third one.
					if (!offers.isEmpty()) {
						offers.remove(offers.size() - 1);
					}
					offers.add(offer(EnchantedBookSources.book(level, enchantment), price(enchantment)));
				}
				villager.addTag(MASTER_MARKER);
			}
			return InteractionResult.PASS;
		});
	}

	private static ResourceKey<Enchantment> masterBook(Villager villager) {
		var profession = villager.getVillagerData().profession();
		if (profession.is(VillagerProfession.TOOLSMITH)) {
			return Enchantments.FORTUNE;
		}
		if (profession.is(VillagerProfession.MASON)) {
			return Enchantments.SILK_TOUCH;
		}
		if (profession.is(VillagerProfession.WEAPONSMITH)) {
			return Enchantments.LOOTING;
		}
		if (profession.is(VillagerProfession.FLETCHER)) {
			return Enchantments.INFINITY;
		}
		if (profession.is(VillagerProfession.ARMORER)) {
			return villager.getRandom().nextBoolean() ? Enchantments.THORNS : null;
		}
		if (profession.is(VillagerProfession.FISHERMAN)) {
			return villager.getRandom().nextBoolean() ? Enchantments.LUCK_OF_THE_SEA : Enchantments.LURE;
		}
		return null;
	}

	private static int price(ResourceKey<Enchantment> enchantment) {
		if (enchantment == Enchantments.FORTUNE || enchantment == Enchantments.SILK_TOUCH
			|| enchantment == Enchantments.LOOTING) {
			return 24;
		}
		return enchantment == Enchantments.THORNS ? 20 : 16;
	}

	private static MerchantOffer offer(ItemStack book, int emeralds) {
		return new MerchantOffer(new ItemCost(Items.EMERALD, emeralds), book, 12, 30, 0.05F);
	}
}
