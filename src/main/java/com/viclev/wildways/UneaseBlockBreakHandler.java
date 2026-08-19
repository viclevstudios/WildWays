package com.viclev.wildways;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class UneaseBlockBreakHandler {
	private static final double EFFECT_RADIUS = 5.0D;
	private static final float SPAWN_CHANCE = 0.1F;

	private UneaseBlockBreakHandler() {
	}

	public static void initialize() {
		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			if (!(level instanceof ServerLevel serverLevel)) {
				return;
			}

			Vec3 blockCenter = Vec3.atCenterOf(pos);
			AABB searchArea = new AABB(pos).inflate(EFFECT_RADIUS);
			boolean shouldSpawnEndermite = serverLevel.getEntitiesOfClass(
				LivingEntity.class,
				searchArea,
				entity -> entity.hasEffect(ModEffects.UNEASE)
					&& entity.distanceToSqr(blockCenter) <= EFFECT_RADIUS * EFFECT_RADIUS
			).stream().anyMatch(entity -> serverLevel.getRandom().nextFloat() < SPAWN_CHANCE);

			if (!shouldSpawnEndermite) {
				return;
			}

			spawnEndermite(serverLevel, pos, blockCenter);
		});
	}

	private static void spawnEndermite(ServerLevel level, BlockPos pos, Vec3 blockCenter) {
		Endermite endermite = EntityTypes.ENDERMITE.create(level, EntitySpawnReason.TRIGGERED);
		if (endermite == null) {
			return;
		}

		endermite.snapTo(blockCenter.x, blockCenter.y, blockCenter.z, level.getRandom().nextFloat() * 360.0F, 0.0F);
		level.addFreshEntity(endermite);
	}
}
