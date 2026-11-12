package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.viclev.wildways.WitherSkulls;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WitherSkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WitherSkullBlock.class)
public abstract class WitherSummoningMixin {
	@Unique
	private static final BlockPattern WILDWAYS_SCULK_FIRST = wildways$mixedWitherPattern("sng");
	@Unique
	private static final BlockPattern WILDWAYS_GOLD_FIRST = wildways$mixedWitherPattern("gns");

	@Unique
	private static BlockPattern wildways$mixedWitherPattern(String skullRow) {
		return BlockPatternBuilder.start()
			.aisle(skullRow, "###", "~#~")
			.where('s', block -> WitherSkulls.SCULK.matches(block.getState()))
			.where('n', block -> block.getState().is(Blocks.WITHER_SKELETON_SKULL) || block.getState().is(Blocks.WITHER_SKELETON_WALL_SKULL))
			.where('g', block -> WitherSkulls.GOLD.matches(block.getState()))
			.where('#', block -> block.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS))
			.where('~', block -> block.getState().isAir())
			.build();
	}

	@WrapOperation(
		method = "checkSpawn(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/SkullBlockEntity;)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z", ordinal = 0)
	)
	private static boolean wildways$acceptVariant(BlockState state, Object block, Operation<Boolean> original) {
		return original.call(state, block) || WitherSkulls.SCULK.matches(state) || WitherSkulls.GOLD.matches(state);
	}

	@WrapOperation(
		method = "checkSpawn(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/SkullBlockEntity;)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/pattern/BlockPattern;find(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/pattern/BlockPattern$BlockPatternMatch;")
	)
	private static BlockPattern.@Nullable BlockPatternMatch wildways$findMixedWither(
		BlockPattern pattern, LevelReader level, BlockPos pos, Operation<BlockPattern.BlockPatternMatch> original
	) {
		BlockPattern.BlockPatternMatch match = original.call(pattern, level, pos);
		if (match == null) {
			// The vanilla search only considers pattern origins in positive directions from the placed skull.
			match = WILDWAYS_SCULK_FIRST.find(level, pos);
			if (match == null) {
				match = WILDWAYS_GOLD_FIRST.find(level, pos);
			}
		}
		return match;
	}
}
