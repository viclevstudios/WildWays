package com.viclev.wildways.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureStart.class)
public abstract class VillageTraderMixin {
	@Inject(method = "placeInChunk", at = @At("TAIL"))
	private void wildways$spawnVillageTrader(WorldGenLevel level, StructureManager structures, ChunkGenerator generator,
		RandomSource random, BoundingBox chunkBounds, ChunkPos chunk, CallbackInfo ci) {
		if (!(level instanceof WorldGenRegion)) {
			return;
		}
		StructureStart start = (StructureStart)(Object)this;
		if (!start.isValid() || !level.registryAccess().lookupOrThrow(Registries.STRUCTURE)
			.wrapAsHolder(start.getStructure()).is(StructureTags.VILLAGE)
			|| !(start.getPieces().getFirst() instanceof PoolElementStructurePiece townCenter)) {
			return;
		}

		BoundingBox centerBounds = townCenter.getBoundingBox();
		BlockPos center = centerBounds.getCenter().atY(centerBounds.minY() + townCenter.getGroundLevelDelta());
		// The town center may span several chunks. Only its center chunk gets a roll.
		if (!chunkBounds.isInside(center) || !level.getLevel().getGameRules().get(GameRules.SPAWN_WANDERING_TRADERS)
			|| !RandomSource.create(level.getSeed() ^ center.asLong() ^ 0x7D61B8C3L).nextBoolean()) {
			return;
		}

		for (BlockPos pos : BlockPos.withinManhattan(center, 12)) {
			if (Math.abs(pos.getX() - center.getX()) > 5 || Math.abs(pos.getY() - center.getY()) > 2
				|| Math.abs(pos.getZ() - center.getZ()) > 5 || !chunkBounds.isInside(pos) || !centerBounds.isInside(pos)
				|| !SpawnPlacements.isSpawnPositionOk(EntityTypes.WANDERING_TRADER, level, pos)
				|| level.getBiome(pos).is(BiomeTags.WITHOUT_WANDERING_TRADER_SPAWNS)
				|| !level.noCollision(EntityTypes.WANDERING_TRADER.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5))) {
				continue;
			}
			var trader = EntityTypes.WANDERING_TRADER.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
			if (trader != null) {
				trader.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
				trader.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.STRUCTURE, null);
				trader.setDespawnDelay(48000);
				trader.setHomeTo(pos.immutable(), 16);
				level.addFreshEntity(trader);
			}
			return;
		}
	}
}
