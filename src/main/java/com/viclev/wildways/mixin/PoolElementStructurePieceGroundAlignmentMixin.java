package com.viclev.wildways.mixin;

import com.viclev.wildways.QuarantineFoundationProcessor;
import com.viclev.wildways.QuarantinePiecePlacementContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
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

	@Inject(method = "postProcess", at = @At("HEAD"))
	private void wildways$alignToLocalTerrain(
		WorldGenLevel level,
		StructureManager structureManager,
		ChunkGenerator generator,
		RandomSource random,
		BoundingBox chunkBounds,
		net.minecraft.world.level.ChunkPos chunkPos,
		BlockPos referencePos,
		CallbackInfo ci
	) {
		if (!wildways$usesQuarantineAlignment()) {
			return;
		}
		if (!wildways$groundAligned) {
			synchronized (this) {
				if (!wildways$groundAligned) {
					int offset = wildways$localGroundOffset(level);
					((PoolElementStructurePiece) (Object) this).move(0, offset, 0);
					wildways$groundAligned = true;
				}
			}
		}
		QuarantinePiecePlacementContext.enter();
	}

	@Inject(method = "postProcess", at = @At("RETURN"))
	private void wildways$finishLocalTerrainAlignment(
		WorldGenLevel level,
		StructureManager structureManager,
		ChunkGenerator generator,
		RandomSource random,
		BoundingBox chunkBounds,
		net.minecraft.world.level.ChunkPos chunkPos,
		BlockPos referencePos,
		CallbackInfo ci
	) {
		if (wildways$usesQuarantineAlignment()) {
			QuarantinePiecePlacementContext.exit();
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
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void wildways$writeGroundAlignment(
		StructurePieceSerializationContext context,
		CompoundTag tag,
		CallbackInfo ci
	) {
		if (wildways$groundAligned) {
			tag.putBoolean("wildways_quarantine_ground_aligned", true);
		}
	}

	@Unique
	private boolean wildways$usesQuarantineAlignment() {
		if (!(((PoolElementStructurePiece) (Object) this).getElement() instanceof SinglePoolElement single)) {
			return false;
		}
		for (StructureProcessor processor : ((SinglePoolElementProcessorAccessor) single).wildways$getProcessors().value().list()) {
			if (processor instanceof QuarantineFoundationProcessor foundationProcessor && foundationProcessor.alignsToGround()) {
				return true;
			}
		}
		return false;
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
