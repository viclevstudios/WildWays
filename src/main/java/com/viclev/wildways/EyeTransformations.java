package com.viclev.wildways;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PotentSulfurBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

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
		ServerTickEvents.END_LEVEL_TICK.register(EyeTransformations::tickConversions);
	}

	public static void transformWithConduit(ServerLevel level, ItemEntity eye) {
		eye.setItem(eye.getItem().transmuteCopy(ModItems.EYE_OF_WATER));
		Vec3 center = eye.position().add(0, 0.15, 0);
		for (int i = 0; i < 12; i++) {
			double angle = i * Math.TAU / 12;
			// Nautilus particles start at the supplied offset and sink as they converge.
			level.sendParticles(ParticleTypes.NAUTILUS, center.x, center.y + 1.2, center.z, 0,
				Math.cos(angle) * 0.55, -1.2 + Math.sin(angle) * 0.12, Math.sin(angle) * 0.55, 1.0);
		}
		burst(level, eye, eye.isInWater() ? ParticleTypes.BUBBLE_COLUMN_UP : ParticleTypes.SPLASH, 14, 0.18, 0.07, 0.08);
		playSound(level, eye, SoundEvents.CONDUIT_ACTIVATE, SoundSource.BLOCKS, 0.75F, 1.35F);
	}

	private static void tickConversions(ServerLevel level) {
		Map<UUID, Integer> warmups = EVOKER_WARMUPS.computeIfAbsent(level, ignored -> new HashMap<>());
		for (var entity : level.getAllEntities()) {
			if (!(entity instanceof ItemEntity itemEntity) || !itemEntity.isAlive()) {
				continue;
			}

			var stack = itemEntity.getItem();
			if (stack.is(ModItems.EYE_OF_VOLCANITE)) {
				var fire = level.getBlockState(itemEntity.blockPosition());
				if (fire.is(BlockTags.FIRE)) {
					itemEntity.setItem(stack.transmuteCopy(ModItems.ATTUNED_EYE_OF_VOLCANITE));
					playVolcaniteEffect(level, itemEntity, fire.is(Blocks.SOUL_FIRE));
				}
				continue;
			}
			if (!stack.is(Items.ENDER_EYE)) {
				continue;
			}
			if (isInSulfurWater(level, itemEntity.blockPosition())) {
				itemEntity.setItem(stack.transmuteCopy(ModItems.EYE_OF_SULFUR));
				burst(level, itemEntity, ParticleTypes.SULFUR_BUBBLES, 22, 0.16, 0.05, 0.08);
				burst(level, itemEntity, ParticleTypes.NOXIOUS_GAS, 5, 0.18, 0.08, 0.02);
				playSound(level, itemEntity, SoundEvents.NOXIOUS_GAS, SoundSource.BLOCKS, 0.8F, 1.2F);
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
				playIllagerEffect(level, itemEntity, evoker);
				warmups.remove(itemEntity.getUUID());
			} else {
				if (remaining % 10 == 0) {
					burst(level, itemEntity, ParticleTypes.WITCH, 3, 0.1, 0.05, 0.02);
				}
				warmups.put(itemEntity.getUUID(), remaining - 1);
			}
		}
		warmups.keySet().removeIf(uuid -> {
			var entity = level.getEntity(uuid);
			return !(entity instanceof ItemEntity itemEntity) || !itemEntity.getItem().is(Items.ENDER_EYE);
		});
	}

	private static void playIllagerEffect(ServerLevel level, ItemEntity eye, Evoker evoker) {
		Vec3 start = evoker.getEyePosition();
		Vec3 end = eye.position().add(0, 0.15, 0);
		for (int step = 1; step <= 10; step++) {
			double progress = step / 11.0;
			Vec3 point = start.lerp(end, progress).add(0, Math.sin(Math.PI * progress) * 0.35, 0);
			level.sendParticles(ParticleTypes.WITCH, point.x, point.y, point.z, 2, 0.06, 0.06, 0.06, 0.02);
		}
		burst(level, eye, ParticleTypes.WITCH, 18, 0.25, 0.16, 0.05);
		playSound(level, eye, SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.0F, 1.0F);
	}

	private static void playVolcaniteEffect(ServerLevel level, ItemEntity eye, boolean soulFire) {
		burst(level, eye, soulFire ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME, 16, 0.19, 0.12, 0.055);
		burst(level, eye, ParticleTypes.SMOKE, 8, 0.13, 0.1, 0.025);
		if (!soulFire) {
			burst(level, eye, ParticleTypes.LAVA, 3, 0.13, 0.07, 0.0);
		}
		playSound(level, eye, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.75F, 0.9F);
	}

	private static void burst(ServerLevel level, ItemEntity eye, ParticleOptions particles, int count, double width, double height, double speed) {
		level.sendParticles(particles, eye.getX(), eye.getY() + 0.15, eye.getZ(), count, width, height, width, speed);
	}

	private static void playSound(ServerLevel level, ItemEntity eye, SoundEvent sound, SoundSource source, float volume, float pitch) {
		level.playSound(null, eye.getX(), eye.getY(), eye.getZ(), sound, source, volume, pitch);
	}

	private static boolean isInSulfurWater(ServerLevel level, BlockPos pos) {
		for (int depth = 0; depth < PotentSulfurBlock.ALLOWED_WATER_BLOCKS_ABOVE; depth++) {
			BlockPos waterPos = pos.below(depth);
			if (!level.getFluidState(waterPos).isSourceOfType(Fluids.WATER)) {
				return false;
			}
			if (level.getBlockState(waterPos.below()).is(Blocks.POTENT_SULFUR)) {
				return true;
			}
		}
		return false;
	}
}
