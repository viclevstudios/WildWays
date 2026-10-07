package com.viclev.wildways;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/** Book rewards that depend on equipment, raid state or the killing blow. */
public final class EnchantedBookDrops {
	private EnchantedBookDrops() {
	}

	public static void initialize() {
		ServerLivingEntityEvents.AFTER_DEATH.register(EnchantedBookDrops::afterDeath);
	}

	private static void afterDeath(LivingEntity entity, DamageSource source) {
		if (!(entity.level() instanceof ServerLevel level)) {
			return;
		}
		if (entity instanceof Zombie zombie && zombie.getMainHandItem().is(ItemTags.SPEARS)) {
			dropWithChance(level, zombie, Enchantments.LUNGE, 0.5F);
		}
		if (entity instanceof Ghast && source.getDirectEntity() instanceof LargeFireball
			&& source.getEntity() instanceof Player) {
			entity.spawnAtLocation(level, EnchantedBookSources.book(level, Enchantments.BLAST_PROTECTION));
		}
		if (entity instanceof Pillager || entity instanceof Vindicator) {
			dropWithChance(level, entity, Enchantments.QUICK_CHARGE, 0.05F);
			dropWithChance(level, entity, Enchantments.MULTISHOT, 0.05F);
			if (entity instanceof net.minecraft.world.entity.raid.Raider raider && raider.hasActiveRaid()) {
				dropWithChance(level, entity, Enchantments.PIERCING, 0.05F);
			}
		}
	}

	private static void dropWithChance(ServerLevel level, LivingEntity entity,
		ResourceKey<Enchantment> enchantment, float chance) {
		if (entity.getRandom().nextFloat() < chance) {
			entity.spawnAtLocation(level, EnchantedBookSources.book(level, enchantment));
		}
	}
}
