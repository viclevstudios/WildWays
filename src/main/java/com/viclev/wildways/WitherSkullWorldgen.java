package com.viclev.wildways;

import com.viclev.wildways.mixin.SinglePoolElementProcessorAccessor;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.ListPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class WitherSkullWorldgen {
	private static final Identifier TREASURE_START = Identifier.withDefaultNamespace("bastion/treasure/big_air_full");
	private static final String TREASURE_ROOM_PREFIX = "bastion/treasure/bases/centers/";
	private static final Identifier PROTECTED_BRIDGE = Identifier.withDefaultNamespace("bastion/bridge/bridge_pieces/bridge");
	private static final long SCULK_SALT = 0x4C7B54EC51F328B1L;
	private static final long GOLD_SALT = 0x7FF620A845AD187DL;

	private WitherSkullWorldgen() {
	}

	public static List<Replacement> choose(WorldGenLevel level, StructureStart start, boolean ancientCity) {
		RandomSource random = RandomSource.create(level.getSeed() ^ start.getChunkPos().pack() ^ (ancientCity ? SCULK_SALT : GOLD_SALT));
		if (ancientCity && random.nextInt(3) != 0) {
			return List.of();
		}

		StructurePiece firstPiece = start.getPieces().getFirst();
		boolean treasureBastion = firstPiece instanceof PoolElementStructurePiece poolPiece
			&& poolPiece.getElement() instanceof SinglePoolElement single && TREASURE_START.equals(single.getTemplateLocation());
		BoundingBox firstBounds = firstPiece.getBoundingBox();
		BlockPos reference = firstBounds.getCenter().atY(firstBounds.minY());
		Set<Replacement> candidates = new LinkedHashSet<>();
		for (StructurePiece piece : start.getPieces()) {
			if (piece instanceof PoolElementStructurePiece poolPiece) {
				collect(level, reference, poolPiece, poolPiece.getElement(), ancientCity, treasureBastion, candidates);
			}
		}
		if (candidates.isEmpty()) {
			return List.of();
		}

		List<Replacement> selected = new ArrayList<>(candidates);
		if (ancientCity || treasureBastion) {
			return List.of(selected.get(random.nextInt(selected.size())));
		}
		selected.removeIf(candidate -> random.nextInt(8) != 0);
		while (selected.size() > 2) {
			selected.remove(random.nextInt(selected.size()));
		}
		return List.copyOf(selected);
	}

	private static void collect(
		WorldGenLevel level, BlockPos reference, PoolElementStructurePiece piece, StructurePoolElement element,
		boolean ancientCity, boolean treasureBastion, Set<Replacement> candidates
	) {
		if (element instanceof ListPoolElement list) {
			for (StructurePoolElement child : list.getElements()) {
				collect(level, reference, piece, child, ancientCity, treasureBastion, candidates);
			}
			return;
		}
		if (!(element instanceof SinglePoolElement single)) {
			return;
		}
		Identifier id = single.getTemplateLocation();
		if (id.equals(PROTECTED_BRIDGE) || treasureBastion && !id.getPath().startsWith(TREASURE_ROOM_PREFIX)) {
			return;
		}

		StructureTemplate template = level.getLevel().getStructureTemplateManager().getOrCreate(id);
		StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(piece.getRotation());
		// Match the vanilla position-based degradation across all pieces, including ones outside this chunk.
		// The final in-chunk block check handles processors that depend on the terrain.
		for (var processor : ((SinglePoolElementProcessorAccessor) single).wildways$getProcessors().value().list()) {
			if (processor instanceof RuleProcessor || processor instanceof BlockRotProcessor) {
				settings.addProcessor(processor);
			}
		}
		if (ancientCity) {
			collectBlocks(level, reference, piece, template, settings, Blocks.SKELETON_SKULL, WitherSkulls.SCULK.skull(), candidates);
			collectBlocks(level, reference, piece, template, settings, Blocks.SKELETON_WALL_SKULL, WitherSkulls.SCULK.wallSkull(), candidates);
		} else {
			collectBlocks(level, reference, piece, template, settings, Blocks.GOLD_BLOCK, WitherSkulls.GOLD.skull(), candidates);
		}
	}

	private static void collectBlocks(
		WorldGenLevel level, BlockPos reference, PoolElementStructurePiece piece, StructureTemplate template,
		StructurePlaceSettings settings, Block source, Block skull, Set<Replacement> candidates
	) {
		// Read unrotated states in template coordinates, as the vanilla processors do.
		BlockPos position = piece.getPosition();
		var blocks = template.filterBlocks(position, new StructurePlaceSettings(), source, false);
		for (var block : StructureTemplate.processBlockInfos(level, position, reference, settings, blocks)) {
			if (block.state().is(source)) {
				candidates.add(new Replacement(block.pos(), source, skull));
			}
		}
	}

	public record Replacement(BlockPos pos, Block source, Block skull) {
		public void place(WorldGenLevel level, BoundingBox chunkBounds) {
			if (chunkBounds.isInside(pos)) {
				BlockState original = level.getBlockState(pos);
				if (original.is(source)) {
					level.setBlock(pos, skull.withPropertiesOf(original), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
				}
			}
		}
	}
}
