package com.viclev.wildways;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class EyeTransformations {
	private static final int EVOKER_RANGE = 8;
	private static final int EVOKER_WARMUP_TICKS = 40;
	private static final Map<ServerLevel, Map<UUID, Integer>> EVOKER_WARMUPS = new WeakHashMap<>();

	private EyeTransformations() {
	}

	public static void initialize() {
		ServerTickEvents.END_LEVEL_TICK.register(EyeTransformations::tickEvokerConversions);
	}

	private static void tickEvokerConversions(ServerLevel level) {
		Map<UUID, Integer> warmups = EVOKER_WARMUPS.computeIfAbsent(level, ignored -> new HashMap<>());
		for (var entity : level.getAllEntities()) {
			if (!(entity instanceof ItemEntity itemEntity) || !itemEntity.getItem().is(Items.ENDER_EYE)) {
				continue;
			}

			Evoker evoker = level.getEntitiesOfClass(
				Evoker.class,
				itemEntity.getBoundingBox().inflate(EVOKER_RANGE),
				candidate -> candidate.isAlive() && !candidate.isCastingSpell()
			).stream().findFirst().orElse(null);
			if (evoker == null) {
				warmups.remove(itemEntity.getUUID());
				continue;
			}

			int remaining = warmups.getOrDefault(itemEntity.getUUID(), EVOKER_WARMUP_TICKS);
			if (remaining == EVOKER_WARMUP_TICKS) {
				level.playSound(null, evoker.blockPosition(), SoundEvents.EVOKER_PREPARE_WOLOLO, SoundSource.HOSTILE, 1.0F, 1.0F);
			}
			evoker.getLookControl().setLookAt(itemEntity, evoker.getMaxHeadYRot(), evoker.getMaxHeadXRot());
			if (remaining <= 1) {
				itemEntity.setItem(itemEntity.getItem().transmuteCopy(ModItems.EYE_OF_ILLAGERS));
				level.playSound(null, itemEntity.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.0F, 1.0F);
				level.sendParticles(ParticleTypes.ENCHANT, itemEntity.getX(), itemEntity.getY() + 0.25, itemEntity.getZ(), 30, 0.35, 0.35, 0.35, 0.1);
				warmups.remove(itemEntity.getUUID());
			} else {
				warmups.put(itemEntity.getUUID(), remaining - 1);
			}
		}
		warmups.keySet().removeIf(uuid -> {
			var entity = level.getEntity(uuid);
			return !(entity instanceof ItemEntity itemEntity) || !itemEntity.getItem().is(Items.ENDER_EYE);
		});
	}
}
