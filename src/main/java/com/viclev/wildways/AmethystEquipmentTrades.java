package com.viclev.wildways;

import java.util.Map;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jspecify.annotations.Nullable;

final class AmethystEquipmentTrades {
	private static final Map<Item, Item> AMETHYST_EQUIVALENTS = Map.of(
		Items.DIAMOND_HELMET, ProgressionItems.AMETHYST_HELMET,
		Items.DIAMOND_CHESTPLATE, ProgressionItems.AMETHYST_CHESTPLATE,
		Items.DIAMOND_LEGGINGS, ProgressionItems.AMETHYST_LEGGINGS,
		Items.DIAMOND_BOOTS, ProgressionItems.AMETHYST_BOOTS,
		Items.DIAMOND_SWORD, ProgressionItems.AMETHYST_SWORD,
		Items.DIAMOND_SPEAR, ProgressionItems.AMETHYST_SPEAR,
		Items.DIAMOND_AXE, ProgressionItems.AMETHYST_AXE,
		Items.DIAMOND_PICKAXE, ProgressionItems.AMETHYST_PICKAXE,
		Items.DIAMOND_SHOVEL, ProgressionItems.AMETHYST_SHOVEL,
		Items.DIAMOND_HOE, ProgressionItems.AMETHYST_HOE
	);
	private static final ResourceKey<VillagerTrade> MASTER_PICKAXE = ResourceKey.create(Registries.VILLAGER_TRADE,
		Identifier.withDefaultNamespace("toolsmith/5/emerald_enchanted_diamond_pickaxe"));
	private static final ResourceKey<VillagerTrade> MASTER_SWORD = ResourceKey.create(Registries.VILLAGER_TRADE,
		Identifier.withDefaultNamespace("weaponsmith/5/emerald_enchanted_diamond_sword"));

	private AmethystEquipmentTrades() {
	}

	static void update(ServerLevel level, Villager villager, boolean masterBookAdded) {
		var profession = villager.getVillagerData().profession();
		if (!profession.is(VillagerProfession.ARMORER) && !profession.is(VillagerProfession.TOOLSMITH)
			&& !profession.is(VillagerProfession.WEAPONSMITH)) {
			return;
		}

		// New default offers use JSON. This also covers saved and experimental diamond offers.
		MerchantOffers offers = villager.getOffers();
		for (int index = 0; index < offers.size(); index++) {
			MerchantOffer old = offers.get(index);
			Item replacement = AMETHYST_EQUIVALENTS.get(old.getResult().getItem());
			if (replacement != null) {
				MerchantOffer updated = new MerchantOffer(old.getItemCostA(), old.getItemCostB(), old.getResult().transmuteCopy(replacement),
					old.getUses(), old.getMaxUses(), old.getXp(), old.getPriceMultiplier(), old.getDemand());
				updated.setSpecialPriceDiff(old.getSpecialPriceDiff());
				offers.set(index, updated);
			}
		}

		Item masterEquipment = masterEquipment(villager);
		if (masterBookAdded && villager.getVillagerData().level() >= 5 && masterEquipment != null
			&& offers.stream().noneMatch(offer -> offer.getResult().is(masterEquipment))) {
			restoreMasterEquipment(level, villager, offers, profession.is(VillagerProfession.TOOLSMITH) ? MASTER_PICKAXE : MASTER_SWORD);
		}
	}

	static @Nullable Item masterEquipment(Villager villager) {
		var profession = villager.getVillagerData().profession();
		if (profession.is(VillagerProfession.TOOLSMITH)) {
			return ProgressionItems.AMETHYST_PICKAXE;
		}
		if (profession.is(VillagerProfession.WEAPONSMITH)) {
			return ProgressionItems.AMETHYST_SWORD;
		}
		return null;
	}

	private static void restoreMasterEquipment(ServerLevel level, Villager villager, MerchantOffers offers, ResourceKey<VillagerTrade> tradeKey) {
		LootParams params = new LootParams.Builder(level)
			.withParameter(LootContextParams.ORIGIN, villager.position())
			.withParameter(LootContextParams.THIS_ENTITY, villager)
			.withParameter(LootContextParams.ADDITIONAL_COST_COMPONENT_ALLOWED, Unit.INSTANCE)
			.create(LootContextParamSets.VILLAGER_TRADE);
		LootContext context = new LootContext.Builder(params).create(Optional.empty());
		level.registryAccess().lookupOrThrow(Registries.VILLAGER_TRADE).getOptional(tradeKey)
			.map(trade -> trade.getOffer(context)).ifPresent(offers::add);
	}
}
