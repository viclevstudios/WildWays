package com.viclev.wildways;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public final class WitherSpawnExplosion extends EntityBasedExplosionDamageCalculator {
	private static final TagKey<Block> FRAGILE = TagKey.create(Registries.BLOCK, Wildways.id("wither_spawn_fragile"));

	public WitherSpawnExplosion(WitherBoss wither) {
		super(wither);
	}

	public static double centerY(WitherBoss wither, double x, double eyeY, double z) {
		BlockGetter level = wither.level();
		BlockPos.MutableBlockPos origin = new BlockPos.MutableBlockPos().set(x, eyeY, z);
		if (canBreak(level, origin, level.getBlockState(origin))) {
			return eyeY;
		}

		// Rays starting inside a protected block lose their energy before reaching the cage below.
		int feetY = wither.getBlockY();
		while (origin.getY() > feetY) {
			origin.setY(origin.getY() - 1);
			if (canBreak(level, origin, level.getBlockState(origin))) {
				return origin.getY() + 0.5;
			}
		}
		return eyeY;
	}

	@Override
	public Optional<Float> getBlockExplosionResistance(
		Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, FluidState fluid
	) {
		if (state.is(FRAGILE) && canBreak(level, pos, state)) {
			return Optional.of(Math.max(1.0F, fluid.getExplosionResistance()));
		}
		return super.getBlockExplosionResistance(explosion, level, pos, state, fluid);
	}

	@Override
	public boolean shouldBlockExplode(Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, float power) {
		return canBreak(level, pos, state) && super.shouldBlockExplode(explosion, level, pos, state, power);
	}

	private static boolean canBreak(BlockGetter level, BlockPos pos, BlockState state) {
		return !state.is(BlockTags.WITHER_IMMUNE) && state.getDestroySpeed(level, pos) >= 0.0F;
	}
}
