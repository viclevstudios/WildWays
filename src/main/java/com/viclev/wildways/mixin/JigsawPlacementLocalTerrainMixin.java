package com.viclev.wildways.mixin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Aligns selected Quarantine Grounds pieces before their bounding boxes are used for terrain adaptation. */
@Mixin(JigsawPlacement.class)
public abstract class JigsawPlacementLocalTerrainMixin {
	@Inject(method = "lambda$addPieces$2", at = @At("TAIL"))
	private static void wildways$alignPiecesBeforeTerrainAdaptation(
		PoolElementStructurePiece startPiece,
		int maxDepth,
		int centerX,
		JigsawStructure.MaxDistance maxDistance,
		int centerY,
		LevelHeightAccessor heightAccessor,
		DimensionPadding dimensionPadding,
		int centerZ,
		BoundingBox startBoundingBox,
		Structure.GenerationContext generationContext,
		boolean useExpansionHack,
		ChunkGenerator chunkGenerator,
		StructureTemplateManager structureTemplateManager,
		WorldgenRandom random,
		Registry<StructureTemplatePool> pools,
		PoolAliasLookup poolAliasLookup,
		LiquidSettings liquidSettings,
		StructurePiecesBuilder builder,
		CallbackInfo ci
	) {
		for (var structurePiece : builder.build().pieces()) {
			if (structurePiece instanceof PoolElementStructurePiece piece && wildways$shouldAlign(piece)) {
				int offsetY = wildways$localGroundOffset(piece, chunkGenerator, heightAccessor, generationContext.randomState(), structureTemplateManager);
				if (offsetY != 0) {
					piece.move(0, offsetY, 0);
					wildways$moveJunctions(piece, offsetY);
				}
			}
		}
	}

	@Unique
	private static void wildways$moveJunctions(PoolElementStructurePiece piece, int offsetY) {
		List<JigsawJunction> junctions = piece.getJunctions();
		for (int index = 0; index < junctions.size(); index++) {
			JigsawJunction junction = junctions.get(index);
			junctions.set(index, new JigsawJunction(
				junction.getSourceX(),
				junction.getSourceGroundY() + offsetY,
				junction.getSourceZ(),
				junction.getDeltaY(),
				junction.getDestProjection()
			));
		}
	}

	@Unique
	private static boolean wildways$shouldAlign(PoolElementStructurePiece piece) {
		if (!(piece.getElement() instanceof net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement element)) {
			return false;
		}
		Identifier location = element.getTemplateLocation();
		if (!location.getNamespace().equals("wildways")) {
			return false;
		}

		String path = location.getPath();
		return path.startsWith("quarantine_grounds/quarantine_grounds_ruin_")
			|| path.startsWith("quarantine_grounds/quarantine_grounds_hospital_house")
			|| path.endsWith("_farm_small")
			|| path.endsWith("_farm_medium")
			|| path.endsWith("_farm_large")
			|| path.endsWith("_grave_small")
			|| path.endsWith("_grave_medium")
			|| path.endsWith("_grave_large");
	}

	@Unique
	private static int wildways$localGroundOffset(
		PoolElementStructurePiece piece,
		ChunkGenerator chunkGenerator,
		LevelHeightAccessor heightAccessor,
		RandomState randomState,
		StructureTemplateManager structureTemplateManager
	) {
		var element = (net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement) piece.getElement();
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
				BlockPos transformed = StructureTemplate.transform(block.pos(), Mirror.NONE, piece.getRotation(), BlockPos.ZERO).offset(piece.getPosition());
				footprint.add(BlockPos.asLong(transformed.getX(), 0, transformed.getZ()));
			}
		}
		if (footprint.isEmpty()) {
			return 0;
		}

		long totalHeight = 0;
		for (long column : footprint) {
			BlockPos pos = BlockPos.of(column);
			totalHeight += chunkGenerator.getFirstFreeHeight(pos.getX(), pos.getZ(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, heightAccessor, randomState) - 1;
		}
		int targetBaseY = (int) Math.floorDiv(totalHeight, footprint.size());
		return targetBaseY - (piece.getPosition().getY() + baseY);
	}
}
