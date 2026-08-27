package com.viclev.wildways;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/** Positions quarantine grounds templates against natural ground and optionally fills exposed supports with earth. */
public final class QuarantineFoundationProcessor implements StructureProcessor {
	private static final Map<StructurePlaceSettings, Integer> ENTITY_OFFSETS = Collections.synchronizedMap(new WeakHashMap<>());
	private static final Map<Object, Map<PlacementKey, GroundProfile>> GROUND_PROFILES = Collections.synchronizedMap(new WeakHashMap<>());
	public static final MapCodec<QuarantineFoundationProcessor> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.BOOL.optionalFieldOf("terrain_following", false).forGetter(processor -> processor.terrainFollowing),
		Codec.BOOL.optionalFieldOf("align_to_ground", false).forGetter(processor -> processor.alignToGround),
		Codec.BOOL.optionalFieldOf("foundation", false).forGetter(processor -> processor.foundation),
		Codec.INT.optionalFieldOf("surface_offset", 0).forGetter(processor -> processor.surfaceOffset),
		Codec.INT.optionalFieldOf("max_foundation_depth", 4).forGetter(processor -> processor.maxFoundationDepth)
	).apply(instance, QuarantineFoundationProcessor::new));

	private final boolean terrainFollowing;
	private final boolean alignToGround;
	private final boolean foundation;
	private final int surfaceOffset;
	private final int maxFoundationDepth;

	public QuarantineFoundationProcessor(boolean terrainFollowing, boolean alignToGround, boolean foundation, int surfaceOffset, int maxFoundationDepth) {
		this.terrainFollowing = terrainFollowing;
		this.alignToGround = alignToGround;
		this.foundation = foundation;
		this.surfaceOffset = surfaceOffset;
		this.maxFoundationDepth = maxFoundationDepth;
	}

	@Override
	public MapCodec<QuarantineFoundationProcessor> codec() {
		return MAP_CODEC;
	}

	@Override
	public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(
		ServerLevelAccessor level,
		BlockPos templateOrigin,
		BlockPos referencePos,
		List<StructureTemplate.StructureBlockInfo> originalBlocks,
		List<StructureTemplate.StructureBlockInfo> processedBlocks,
		StructurePlaceSettings settings
	) {
		ENTITY_OFFSETS.remove(settings);
		Bounds bounds = Bounds.from(processedBlocks);
		if (bounds == null) {
			return processedBlocks;
		}
		GroundProfile groundProfile = groundProfile(level, templateOrigin, processedBlocks, bounds);

		List<StructureTemplate.StructureBlockInfo> positionedBlocks;
		if (terrainFollowing) {
			positionedBlocks = followTerrain(level, processedBlocks, bounds, groundProfile);
		} else if (alignToGround) {
			int offsetY = findAverageGroundY(groundProfile, processedBlocks, bounds) + surfaceOffset - bounds.minY;
			positionedBlocks = moveAll(processedBlocks, offsetY);
			if (offsetY != 0) {
				ENTITY_OFFSETS.put(settings, offsetY);
			}
		} else {
			positionedBlocks = processedBlocks;
		}

		if (!foundation) {
			return positionedBlocks;
		}

		Bounds positionedBounds = Bounds.from(positionedBlocks);
		if (positionedBounds == null) {
			return positionedBlocks;
		}
		int foundationY = positionedBounds.minY;
		Map<Long, Integer> baseColumns = new java.util.HashMap<>();
		Set<BlockPos> occupiedPositions = new HashSet<>();
		for (StructureTemplate.StructureBlockInfo blockInfo : positionedBlocks) {
			occupiedPositions.add(blockInfo.pos());
			if (!blockInfo.state().isAir() && blockInfo.pos().getY() == foundationY) {
				long column = BlockPos.asLong(blockInfo.pos().getX(), 0, blockInfo.pos().getZ());
				baseColumns.put(column, blockInfo.pos().getY());
			}
		}

		List<StructureTemplate.StructureBlockInfo> withFoundations = new ArrayList<>(positionedBlocks);
		for (Map.Entry<Long, Integer> entry : baseColumns.entrySet()) {
			BlockPos columnPos = BlockPos.of(entry.getKey());
			int groundY = groundProfile.heightAt(columnPos.getX(), columnPos.getZ());
			int lowestFoundationY = Math.max(groundY + 1, entry.getValue() - maxFoundationDepth);
			for (int y = entry.getValue() - 1; y >= lowestFoundationY; y--) {
				BlockPos supportPos = new BlockPos(columnPos.getX(), y, columnPos.getZ());
				if (!occupiedPositions.contains(supportPos)) {
					BlockState foundationBlock = y == entry.getValue() - 1 && isFoundationEdge(baseColumns, columnPos)
						? Blocks.GRASS_BLOCK.defaultBlockState()
						: Blocks.DIRT.defaultBlockState();
					withFoundations.add(new StructureTemplate.StructureBlockInfo(supportPos, foundationBlock, null));
				}
			}
		}

		return withFoundations;
	}

	public static int takeEntityOffset(StructurePlaceSettings settings) {
		Integer offset = ENTITY_OFFSETS.remove(settings);
		return offset == null ? 0 : offset;
	}

	private static List<StructureTemplate.StructureBlockInfo> moveAll(List<StructureTemplate.StructureBlockInfo> blocks, int offsetY) {
		List<StructureTemplate.StructureBlockInfo> movedBlocks = new ArrayList<>(blocks.size());
		for (StructureTemplate.StructureBlockInfo blockInfo : blocks) {
			movedBlocks.add(move(blockInfo, blockInfo.pos().offset(0, offsetY, 0)));
		}
		return movedBlocks;
	}

	private static StructureTemplate.StructureBlockInfo move(StructureTemplate.StructureBlockInfo blockInfo, BlockPos targetPos) {
		return new StructureTemplate.StructureBlockInfo(targetPos, blockInfo.state(), blockInfo.nbt());
	}

	/**
	 * Moves the path surface per column, while moving lamps and other raised decoration as one connected unit.
	 * A gravity-style processor applied to every block separately breaks hanging lanterns on sloped terrain.
	 */
	private List<StructureTemplate.StructureBlockInfo> followTerrain(
		ServerLevelAccessor level,
		List<StructureTemplate.StructureBlockInfo> blocks,
		Bounds bounds,
		GroundProfile groundProfile
	) {
		Map<BlockPos, StructureTemplate.StructureBlockInfo> aboveSurface = new java.util.HashMap<>();
		List<StructureTemplate.StructureBlockInfo> movedBlocks = new ArrayList<>(blocks.size());
		for (StructureTemplate.StructureBlockInfo blockInfo : blocks) {
			if (blockInfo.state().isAir()) {
				movedBlocks.add(blockInfo);
			} else if (blockInfo.pos().getY() <= bounds.minY) {
				int groundY = groundProfile.heightAt(blockInfo.pos().getX(), blockInfo.pos().getZ());
				int relativeY = blockInfo.pos().getY() - bounds.minY;
				movedBlocks.add(move(blockInfo, new BlockPos(blockInfo.pos().getX(), groundY + surfaceOffset + relativeY, blockInfo.pos().getZ())));
			} else {
				aboveSurface.put(blockInfo.pos(), blockInfo);
			}
		}

		Set<BlockPos> visited = new HashSet<>();
		for (BlockPos start : aboveSurface.keySet()) {
			if (!visited.add(start)) {
				continue;
			}
			List<StructureTemplate.StructureBlockInfo> decoration = new ArrayList<>();
			Deque<BlockPos> pending = new ArrayDeque<>();
			pending.add(start);
			while (!pending.isEmpty()) {
				BlockPos position = pending.removeFirst();
				StructureTemplate.StructureBlockInfo blockInfo = aboveSurface.get(position);
				if (blockInfo == null) {
					continue;
				}
				decoration.add(blockInfo);
				for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
					BlockPos neighbour = position.relative(direction);
					if (aboveSurface.containsKey(neighbour) && visited.add(neighbour)) {
						pending.add(neighbour);
					}
				}
			}

			BlockPos anchor = nearestSurfaceColumn(decoration, blocks, bounds.minY);
			int offsetY = groundProfile.heightAt(anchor.getX(), anchor.getZ()) + surfaceOffset - bounds.minY;
			for (StructureTemplate.StructureBlockInfo blockInfo : decoration) {
				movedBlocks.add(move(blockInfo, blockInfo.pos().offset(0, offsetY, 0)));
			}
		}
		return movedBlocks;
	}

	private static BlockPos nearestSurfaceColumn(
		List<StructureTemplate.StructureBlockInfo> decoration,
		List<StructureTemplate.StructureBlockInfo> blocks,
		int baseY
	) {
		BlockPos nearest = null;
		int bestDistance = Integer.MAX_VALUE;
		for (StructureTemplate.StructureBlockInfo surface : blocks) {
			if (surface.state().isAir() || surface.pos().getY() != baseY) {
				continue;
			}
			for (StructureTemplate.StructureBlockInfo decorativeBlock : decoration) {
				int dx = surface.pos().getX() - decorativeBlock.pos().getX();
				int dz = surface.pos().getZ() - decorativeBlock.pos().getZ();
				int distance = dx * dx + dz * dz;
				if (distance < bestDistance) {
					bestDistance = distance;
					nearest = surface.pos();
				}
			}
		}
		return nearest == null ? decoration.getFirst().pos() : nearest;
	}

	private static boolean isFoundationEdge(Map<Long, Integer> baseColumns, BlockPos columnPos) {
		return !baseColumns.containsKey(BlockPos.asLong(columnPos.getX() + 1, 0, columnPos.getZ()))
			|| !baseColumns.containsKey(BlockPos.asLong(columnPos.getX() - 1, 0, columnPos.getZ()))
			|| !baseColumns.containsKey(BlockPos.asLong(columnPos.getX(), 0, columnPos.getZ() + 1))
			|| !baseColumns.containsKey(BlockPos.asLong(columnPos.getX(), 0, columnPos.getZ() - 1));
	}

	private static int findGroundY(ServerLevelAccessor level, int x, int z) {
		int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
		while (y >= level.getMinY() && isTreeBlock(level.getBlockState(new BlockPos(x, y, z)))) {
			y--;
		}
		return y;
	}

	/** Uses the same average-ground-level idea as vanilla village pieces, but ignores canopy blocks. */
	private static int findAverageGroundY(
		GroundProfile groundProfile,
		List<StructureTemplate.StructureBlockInfo> blocks,
		Bounds bounds
	) {
		Set<Long> footprint = new HashSet<>();
		for (StructureTemplate.StructureBlockInfo blockInfo : blocks) {
			if (!blockInfo.state().isAir() && blockInfo.pos().getY() == bounds.minY) {
				footprint.add(BlockPos.asLong(blockInfo.pos().getX(), 0, blockInfo.pos().getZ()));
			}
		}
		if (footprint.isEmpty()) {
			return groundProfile.heightAt((bounds.minX + bounds.maxX) / 2, (bounds.minZ + bounds.maxZ) / 2);
		}

		long totalHeight = 0;
		for (long column : footprint) {
			BlockPos pos = BlockPos.of(column);
			totalHeight += groundProfile.heightAt(pos.getX(), pos.getZ());
		}
		return (int) Math.floorDiv(totalHeight, footprint.size());
	}

	/**
	 * Structure templates are placed once per intersecting chunk. Capturing the natural ground profile on
	 * the first pass prevents blocks placed in an earlier chunk from changing the height of later passes.
	 */
	private static GroundProfile groundProfile(
		ServerLevelAccessor level,
		BlockPos templateOrigin,
		List<StructureTemplate.StructureBlockInfo> blocks,
		Bounds bounds
	) {
		Object worldKey = level instanceof net.minecraft.server.level.WorldGenRegion region ? region.getLevel() : level;
		PlacementKey key = new PlacementKey(templateOrigin.immutable(), bounds);
		synchronized (GROUND_PROFILES) {
			Map<PlacementKey, GroundProfile> profiles = GROUND_PROFILES.computeIfAbsent(worldKey, ignored -> new java.util.HashMap<>());
			return profiles.computeIfAbsent(key, ignored -> GroundProfile.capture(level, blocks));
		}
	}

	private static boolean isTreeBlock(BlockState state) {
		return state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS) || state.getBlock() instanceof HugeMushroomBlock;
	}

	private record Bounds(int minX, int maxX, int minY, int minZ, int maxZ) {
		private static Bounds from(List<StructureTemplate.StructureBlockInfo> blocks) {
			int minX = Integer.MAX_VALUE;
			int maxX = Integer.MIN_VALUE;
			int minY = Integer.MAX_VALUE;
			int minZ = Integer.MAX_VALUE;
			int maxZ = Integer.MIN_VALUE;

			for (StructureTemplate.StructureBlockInfo blockInfo : blocks) {
				if (blockInfo.state().isAir()) {
					continue;
				}
				minX = Math.min(minX, blockInfo.pos().getX());
				maxX = Math.max(maxX, blockInfo.pos().getX());
				minY = Math.min(minY, blockInfo.pos().getY());
				minZ = Math.min(minZ, blockInfo.pos().getZ());
				maxZ = Math.max(maxZ, blockInfo.pos().getZ());
			}

			return minY == Integer.MAX_VALUE ? null : new Bounds(minX, maxX, minY, minZ, maxZ);
		}
	}

	/**
	 * The template origin and its bounds identify a jigsaw piece. The reference position deliberately does
	 * not belong here: Minecraft changes it for every chunk passed to PoolElementStructurePiece#postProcess.
	 */
	private record PlacementKey(BlockPos templateOrigin, Bounds bounds) {
	}

	private record GroundProfile(Map<Long, Integer> heights) {
		private static GroundProfile capture(ServerLevelAccessor level, List<StructureTemplate.StructureBlockInfo> blocks) {
			Map<Long, Integer> heights = new java.util.HashMap<>();
			for (StructureTemplate.StructureBlockInfo blockInfo : blocks) {
				if (!blockInfo.state().isAir()) {
					long column = BlockPos.asLong(blockInfo.pos().getX(), 0, blockInfo.pos().getZ());
					heights.computeIfAbsent(column, ignored -> findGroundY(level, blockInfo.pos().getX(), blockInfo.pos().getZ()));
				}
			}
			return new GroundProfile(Map.copyOf(heights));
		}

		private int heightAt(int x, int z) {
			Integer height = heights.get(BlockPos.asLong(x, 0, z));
			if (height == null) {
				throw new IllegalStateException("Missing cached terrain height for structure column " + x + "," + z);
			}
			return height;
		}
	}
}
