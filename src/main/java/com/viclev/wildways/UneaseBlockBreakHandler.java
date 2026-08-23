package com.viclev.wildways;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.phys.Vec3;

public final class UneaseBlockBreakHandler {
	private static final float SPAWN_CHANCE = 0.1F;

	private UneaseBlockBreakHandler() {
	}

	public static void initialize() {
		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			if (!(level instanceof ServerLevel serverLevel)) {
				return;
			}

			MobEffectInstance unease = player.getEffect(ModEffects.UNEASE);
			if (unease == null) {
				return;
			}

			float spawnChance = getSpawnChance(unease);
			if (serverLevel.getRandom().nextFloat() >= spawnChance) {
				return;
			}

			spawnEndermite(serverLevel, pos);
		});
	}

	public static float getSpawnChance(MobEffectInstance unease) {
		return Math.min(1.0F, SPAWN_CHANCE * (unease.getAmplifier() + 1));
	}

	public static void spawnEndermite(ServerLevel level, BlockPos pos) {
		Endermite endermite = EntityTypes.ENDERMITE.create(level, EntitySpawnReason.TRIGGERED);
		if (endermite == null) {
			return;
		}

		Vec3 blockCenter = Vec3.atCenterOf(pos);
		endermite.snapTo(blockCenter.x, blockCenter.y, blockCenter.z, level.getRandom().nextFloat() * 360.0F, 0.0F);
		level.addFreshEntity(endermite);
	}
}
