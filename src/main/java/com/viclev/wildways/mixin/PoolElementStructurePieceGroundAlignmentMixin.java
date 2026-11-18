package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.viclev.wildways.QuarantineFoundationProcessor;
import com.viclev.wildways.QuarantinePiecePlacementContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes the piece position and its bounding box agree with the local terrain before Minecraft clips
 * template placement to each intersecting chunk.
 */
@Mixin(PoolElementStructurePiece.class)
public abstract class PoolElementStructurePieceGroundAlignmentMixin {
	@Shadow
	@Final
	private StructureTemplateManager structureTemplateManager;

	@Shadow
	@Final
	protected Rotation rotation;

	@Shadow
	protected BlockPos position;

	@Unique
	private boolean wildways$groundAligned;

	@Unique
	private @Nullable QuarantinePiecePlacementContext wildways$terrain;

	@WrapMethod(method = "postProcess")
	private void wildways$placeWithTerrain(
		WorldGenLevel level,
		StructureManager structureManager,
		ChunkGenerator generator,
		RandomSource random,
		BoundingBox chunkBounds,
		ChunkPos chunkPos,
		BlockPos referencePos,
		Operation<Void> original
	) {
		var foundation = this.wildways$foundation();
		if (foundation == null) {
			original.call(level, structureManager, generator, random, chunkBounds, chunkPos, referencePos);
			return;
		}
		var piece = (PoolElementStructurePiece) (Object) this;
		synchronized (this) {
			boolean align = foundation.alignsToGround() && !this.wildways$groundAligned;
			boolean wasPending = this.wildways$terrain != null;
			if (align) {
				piece.move(0, this.wildways$localGroundOffset(level), 0);
				this.wildways$groundAligned = true;
			}
			if (this.wildways$terrain == null) {
				this.wildways$terrain = new QuarantinePiecePlacementContext(piece.getBoundingBox());
			}
			this.wildways$terrain.place(() -> original.call(level, structureManager, generator, random, chunkBounds, chunkPos, referencePos));
			if (this.wildways$terrain.finishChunk(chunkPos)) {
				this.wildways$terrain = null;
			}
			if (align || wasPending || this.wildways$terrain != null) {
				this.wildways$markOwnerUnsaved(level, structureManager, chunkPos, piece);
			}
		}
	}

	@Unique
	private void wildways$markOwnerUnsaved(WorldGenLevel level, StructureManager structures, ChunkPos chunk, PoolElementStructurePiece piece) {
		if (level instanceof WorldGenRegion region) {
			for (var start : structures.startsForStructure(chunk.x(), chunk.z(), structure -> true)) {
				if (start.getPieces().contains(piece)) {
					ChunkPos owner = start.getChunkPos();
					region.getChunk(owner.x(), owner.z(), ChunkStatus.STRUCTURE_STARTS).markUnsaved();
					return;
				}
			}
		}
	}

	@Inject(
		method = "<init>(Lnet/minecraft/world/level/levelgen/structure/pieces/StructurePieceSerializationContext;Lnet/minecraft/nbt/CompoundTag;)V",
		at = @At("RETURN")
	)
	private void wildways$readGroundAlignment(
		StructurePieceSerializationContext context,
		CompoundTag tag,
		CallbackInfo ci
	) {
		wildways$groundAligned = tag.getBooleanOr("wildways_quarantine_ground_aligned", false);
		wildways$terrain = tag.getCompound("wildways_quarantine_terrain").map(QuarantinePiecePlacementContext::load).orElse(null);
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void wildways$writeGroundAlignment(
		StructurePieceSerializationContext context,
		CompoundTag tag,
		CallbackInfo ci
	) {
		synchronized (this) {
			if (this.wildways$groundAligned) {
				tag.putBoolean("wildways_quarantine_ground_aligned", true);
			}
			if (this.wildways$terrain != null) {
				tag.put("wildways_quarantine_terrain", this.wildways$terrain.save());
			}
		}
	}

	@Unique
	private @Nullable QuarantineFoundationProcessor wildways$foundation() {
		if (!(((PoolElementStructurePiece) (Object) this).getElement() instanceof SinglePoolElement single)) {
			return null;
		}
		for (StructureProcessor processor : ((SinglePoolElementProcessorAccessor) single).wildways$getProcessors().value().list()) {
			if (processor instanceof QuarantineFoundationProcessor foundationProcessor) {
				return foundationProcessor;
			}
		}
		return null;
	}

	@Unique
	private int wildways$localGroundOffset(WorldGenLevel level) {
		SinglePoolElement element = (SinglePoolElement) ((PoolElementStructurePiece) (Object) this).getElement();
		StructureTemplate template = structureTemplateManager.getOrCreate(element.getTemplateLocation());
		List<StructureTemplate.Palette> palettes = ((StructureTemplatePaletteAccessor) template).wildways$getPalettes();
		if (palettes.isEmpty()) {
			return 0;
		}

		List<StructureTemplate.StructureBlockInfo> blocks = palettes.getFirst().blocks();
		int baseY = Integer.MAX_VALUE;
		for (StructureTemplate.StructureBlockInfo block : blocks) {
			if (!block.state().isAir()) {
				baseY = Math.min(baseY, block.pos().getY());
			}
		}
		if (baseY == Integer.MAX_VALUE) {
			return 0;
		}

		Set<Long> footprint = new HashSet<>();
		for (StructureTemplate.StructureBlockInfo block : blocks) {
			if (!block.state().isAir() && block.pos().getY() == baseY) {
				BlockPos transformed = StructureTemplate.transform(block.pos(), Mirror.NONE, rotation, BlockPos.ZERO).offset(position);
				footprint.add(BlockPos.asLong(transformed.getX(), 0, transformed.getZ()));
			}
		}
		if (footprint.isEmpty()) {
			return 0;
		}

		long totalHeight = 0;
		for (long column : footprint) {
			BlockPos pos = BlockPos.of(column);
			totalHeight += QuarantineFoundationProcessor.findNaturalGroundY(level, pos.getX(), pos.getZ());
		}
		int targetBaseY = (int) Math.floorDiv(totalHeight, footprint.size());
		return targetBaseY - (position.getY() + baseY);
	}
}
