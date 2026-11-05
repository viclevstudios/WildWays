package com.viclev.wildways;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.GeyserParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PotentSulfurBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

public final class EyeTransformations {
	private static final int EVOKER_RANGE = 8;
	private static final int EVOKER_WARMUP_TICKS = 40;
	private static final int WATER_BUBBLE_TICKS = 32;
	private static final int SULFUR_ERUPTION_DELAY = 6;
	private static final int VOLCANITE_RISE_DELAY_TICKS = 10;
	private static final int VOLCANITE_RISE_TICKS = 40;
	private static final double VOLCANITE_RISE_HEIGHT = 2.0;
	private static final Map<ServerLevel, Map<UUID, Integer>> EVOKER_WARMUPS = new WeakHashMap<>();
	private static final Map<ServerLevel, LevelEffects> ACTIVE_EFFECTS = new WeakHashMap<>();
	private static final AttachmentType<VolcaniteRise> VOLCANITE_RISE = AttachmentRegistry.create(
		Wildways.id("volcanite_rise"),
		builder -> builder.syncWith(StreamCodec.composite(
			ByteBufCodecs.optional(ByteBufCodecs.DOUBLE), VolcaniteRise::startY,
			ByteBufCodecs.VAR_LONG, VolcaniteRise::startTick,
			ByteBufCodecs.BOOL, VolcaniteRise::soulFire,
			VolcaniteRise::new
		), AttachmentSyncPredicate.all())
	);

	private static final class LevelEffects {
		private final List<WaterBubbles> waterBubbles = new ArrayList<>();
		private final Map<BlockPos, Long> sulfurEruptions = new HashMap<>();
	}

	private record WaterBubbles(Vec3 origin, long startTick) {
	}

	private record VolcaniteRise(Optional<Double> startY, long startTick, boolean soulFire) {
	}

	private EyeTransformations() {
	}

	public static void initialize() {
		ServerTickEvents.END_LEVEL_TICK.register(EyeTransformations::tickConversions);
	}

	public static boolean isVolcaniteRising(ItemEntity eye) {
		VolcaniteRise rise = eye.getAttached(VOLCANITE_RISE);
		return rise != null && rise.startY().isPresent();
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
		if (eye.isInWater()) {
			double bubbleY = Math.max(eye.blockPosition().getY() + 0.08, eye.getY() - 0.38);
			Vec3 bubbleOrigin = new Vec3(eye.getX(), bubbleY, eye.getZ());
			ACTIVE_EFFECTS.computeIfAbsent(level, ignored -> new LevelEffects()).waterBubbles
				.add(new WaterBubbles(bubbleOrigin, level.getGameTime()));
		}
		playSound(level, eye, SoundEvents.CONDUIT_ACTIVATE, SoundSource.BLOCKS, 0.75F, 1.35F);
	}

	private static void tickConversions(ServerLevel level) {
		LevelEffects effects = ACTIVE_EFFECTS.computeIfAbsent(level, ignored -> new LevelEffects());
		tickWaterBubbles(level, effects.waterBubbles);
		tickSulfurEruptions(level, effects.sulfurEruptions);
		Map<UUID, Integer> warmups = EVOKER_WARMUPS.computeIfAbsent(level, ignored -> new HashMap<>());
		for (var entity : level.getAllEntities()) {
			if (!(entity instanceof ItemEntity itemEntity) || !itemEntity.isAlive()) {
				continue;
			}

			var stack = itemEntity.getItem();
			if (stack.is(ModItems.EYE_OF_VOLCANITE)) {
				tickVolcaniteRise(level, itemEntity);
				continue;
			}
			if (itemEntity.hasAttached(VOLCANITE_RISE)) {
				itemEntity.removeAttached(VOLCANITE_RISE);
			}
			if (!stack.is(Items.ENDER_EYE)) {
				continue;
			}
			BlockPos sulfur = findSulfurBelow(level, itemEntity.blockPosition());
			if (sulfur != null) {
				itemEntity.setItem(stack.transmuteCopy(ModItems.EYE_OF_SULFUR));
				effects.sulfurEruptions.putIfAbsent(sulfur, level.getGameTime() + SULFUR_ERUPTION_DELAY);
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

	private static void tickWaterBubbles(ServerLevel level, List<WaterBubbles> bubbles) {
		bubbles.removeIf(effect -> {
			long elapsed = level.getGameTime() - effect.startTick();
			Vec3 origin = effect.origin();
			BlockPos pos = BlockPos.containing(origin);
			if (elapsed > WATER_BUBBLE_TICKS || !level.isLoaded(pos) || !level.getFluidState(pos).is(FluidTags.WATER)) {
				return true;
			}
			if (elapsed > 0 && elapsed % 5 == 0) {
				level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, origin.x, origin.y, origin.z, 5, 0.13, 0.025, 0.13, 0.035);
			}
			return false;
		});
	}

	private static void tickSulfurEruptions(ServerLevel level, Map<BlockPos, Long> eruptions) {
		eruptions.entrySet().removeIf(eruption -> {
			if (level.getGameTime() < eruption.getValue()) {
				return false;
			}
			BlockPos sulfur = eruption.getKey();
			if (level.isLoaded(sulfur) && level.getBlockState(sulfur).is(Blocks.POTENT_SULFUR)
				&& level.getFluidState(sulfur.above()).isSourceOfType(Fluids.WATER)) {
				playSulfurEruption(level, sulfur);
			}
			return true;
		});
	}

	private static void tickVolcaniteRise(ServerLevel level, ItemEntity eye) {
		VolcaniteRise rise = eye.getAttached(VOLCANITE_RISE);
		if (rise == null) {
			var fire = level.getBlockState(eye.blockPosition());
			if (!fire.is(BlockTags.FIRE)) {
				return;
			}
			rise = new VolcaniteRise(Optional.empty(), level.getGameTime(), fire.is(Blocks.SOUL_FIRE));
			eye.setAttached(VOLCANITE_RISE, rise);
			eye.setPickUpDelay(VOLCANITE_RISE_DELAY_TICKS + VOLCANITE_RISE_TICKS + 2);
		}

		long elapsed = level.getGameTime() - rise.startTick();
		long liftTick = elapsed - VOLCANITE_RISE_DELAY_TICKS;
		if (liftTick >= VOLCANITE_RISE_TICKS) {
			eye.removeAttached(VOLCANITE_RISE);
			eye.setDeltaMovement(Vec3.ZERO);
			eye.needsSync = true;
			eye.setItem(eye.getItem().transmuteCopy(ModItems.ATTUNED_EYE_OF_VOLCANITE));
			playVolcaniteEffect(level, eye, rise.soulFire());
			return;
		}

		if (liftTick >= 0) {
			if (rise.startY().isEmpty()) {
				rise = new VolcaniteRise(Optional.of(eye.getY()), rise.startTick(), rise.soulFire());
				eye.setAttached(VOLCANITE_RISE, rise);
			}
			double progress = (liftTick + 1.0) / VOLCANITE_RISE_TICKS;
			double nextY = rise.startY().orElseThrow() + VOLCANITE_RISE_HEIGHT * progress;
			double lift = Mth.clamp(nextY - eye.getY(), 0.0, 0.12);
			// Compensate for gravity while leaving block collision to the item entity.
			eye.setDeltaMovement(0, eye.getGravity() + lift, 0);
			eye.needsSync = true;
		}

		double angle = elapsed * Math.TAU * 3 / VOLCANITE_RISE_TICKS;
		double x = Math.cos(angle) * 0.35;
		double z = Math.sin(angle) * 0.35;
		ParticleOptions flame = rise.soulFire() ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME;
		level.sendParticles(flame, eye.getX() + x, eye.getY() + 0.15, eye.getZ() + z, 0, 0, 0, 0, 0);
		level.sendParticles(flame, eye.getX() - x, eye.getY() + 0.15, eye.getZ() - z, 0, 0, 0, 0, 0);
	}

	private static void playSulfurEruption(ServerLevel level, BlockPos sulfur) {
		Vec3 base = Vec3.atBottomCenterOf(sulfur.above());
		level.sendParticles(ParticleTypes.SULFUR_BUBBLES, base.x, base.y + 0.1, base.z, 24, 0.18, 0.04, 0.18, 0.06);

		BlockPos surface = sulfur.above();
		int depth = 0;
		while (depth < PotentSulfurBlock.ALLOWED_WATER_BLOCKS_ABOVE && level.getFluidState(surface).isSourceOfType(Fluids.WATER)) {
			depth++;
			surface = surface.above();
		}
		if (level.getFluidState(surface).isEmpty() && level.getBlockState(surface).getCollisionShape(level, surface).isEmpty()) {
			Vec3 plume = Vec3.atBottomCenterOf(surface);
			level.sendParticles(new GeyserParticleOptions(ParticleTypes.GEYSER, depth), plume.x, plume.y, plume.z, 0, 0, 0, 0, 0);
		}
		level.playSound(null, surface, SoundEvents.GEYSER_ERUPTION_START, SoundSource.BLOCKS, 0.9F, 1.1F);
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

	private static BlockPos findSulfurBelow(ServerLevel level, BlockPos pos) {
		for (int depth = 0; depth < PotentSulfurBlock.ALLOWED_WATER_BLOCKS_ABOVE; depth++) {
			BlockPos waterPos = pos.below(depth);
			if (!level.getFluidState(waterPos).isSourceOfType(Fluids.WATER)) {
				return null;
			}
			if (level.getBlockState(waterPos.below()).is(Blocks.POTENT_SULFUR)) {
				return waterPos.below();
			}
		}
		return null;
	}
}
