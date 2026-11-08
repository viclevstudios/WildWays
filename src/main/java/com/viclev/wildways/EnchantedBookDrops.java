package com.viclev.wildways;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/** Book rewards that depend on equipment, raid state or the killing blow. */
public final class EnchantedBookDrops {
	private EnchantedBookDrops() {
	}

	public static void initialize() {
		LootTableEvents.MODIFY_DROPS.register((table, context, drops) -> {
			DamageSource source = context.getOptional(LootContextParams.DAMAGE_SOURCE);
			if (source != null && source.getEntity() instanceof Player
				&& context.getOptional(LootContextParams.THIS_ENTITY) instanceof Zombie zombie
				&& zombie.getLootTable().filter(table::is).isPresent()
				&& zombie.getMainHandItem().is(ItemTags.SPEARS) && context.getRandom().nextFloat() < 0.5F) {
				drops.add(EnchantedBookSources.book(context.getLevel(), Enchantments.LUNGE));
			}
		});
		ServerLivingEntityEvents.AFTER_DEATH.register(EnchantedBookDrops::afterDeath);
	}

	public static void dropRaidBooksBeforeLeavingRaid(Raider raider, DamageSource source) {
		if (raider.level() instanceof ServerLevel level && level.getGameRules().get(GameRules.MOB_DROPS)
			&& source.getEntity() instanceof Player && raider.hasActiveRaid()
			&& (raider instanceof Pillager || raider instanceof Vindicator)) {
			dropWithChance(level, raider, Enchantments.QUICK_CHARGE, 0.05F);
			dropWithChance(level, raider, Enchantments.MULTISHOT, 0.05F);
			dropWithChance(level, raider, Enchantments.PIERCING, 0.05F);
		}
	}

	private static void afterDeath(LivingEntity entity, DamageSource source) {
		if (!(entity.level() instanceof ServerLevel level)) {
			return;
		}
		if (!(source.getEntity() instanceof Player)) {
			return;
		}
		if (entity instanceof Ghast && source.getDirectEntity() instanceof LargeFireball
			&& source.getEntity() instanceof Player) {
			dropWithChance(level, entity, Enchantments.BLAST_PROTECTION, 0.5F);
		}
	}

	private static void dropWithChance(ServerLevel level, LivingEntity entity,
		ResourceKey<Enchantment> enchantment, float chance) {
		if (entity.getRandom().nextFloat() < chance) {
			entity.spawnAtLocation(level, EnchantedBookSources.book(level, enchantment));
		}
	}
}
