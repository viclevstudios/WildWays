package com.viclev.wildways;

import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;

public class WitherSkullGameTests {
	@GameTest
	public void mixedSkullsSummonWithGoldPlacedLast(GameTestHelper helper) {
		placeSkulls(helper, WitherSkulls.SCULK.skull(), Blocks.WITHER_SKELETON_SKULL, WitherSkulls.GOLD.skull(), 2);
		helper.assertEntityPresent(EntityTypes.WITHER);
		helper.assertBlockPresent(Blocks.AIR, 2, 2, 2);
		helper.succeed();
	}

	@GameTest
	public void reversedMixedSkullsSummonWithVanillaPlacedLast(GameTestHelper helper) {
		placeSkulls(helper, WitherSkulls.GOLD.skull(), Blocks.WITHER_SKELETON_SKULL, WitherSkulls.SCULK.skull(), 1);
		helper.assertEntityPresent(EntityTypes.WITHER);
		helper.succeed();
	}

	@GameTest
	public void mixedSkullsRequireBothVariantsAndVanillaInTheMiddle(GameTestHelper helper) {
		placeSkulls(helper, WitherSkulls.SCULK.skull(), Blocks.WITHER_SKELETON_SKULL, WitherSkulls.SCULK.skull(), 2);
		helper.assertEntityNotPresent(EntityTypes.WITHER);
		placeSkulls(helper, Blocks.WITHER_SKELETON_SKULL, WitherSkulls.SCULK.skull(), WitherSkulls.GOLD.skull(), 2);
		helper.assertEntityNotPresent(EntityTypes.WITHER);
		helper.succeed();
	}

	@GameTest
	public void threeVanillaSkullsStillSummon(GameTestHelper helper) {
		Block skull = Blocks.WITHER_SKELETON_SKULL;
		placeSkulls(helper, skull, skull, skull, 2);
		helper.assertEntityPresent(EntityTypes.WITHER);
		helper.succeed();
	}

	@GameTest
	public void treasureReplacesOneGoldBlockInTheTreasureRoom(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		var bastion = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.BASTION_REMNANT).value();
		var start = piece(level, "bastion/treasure/big_air_full", BlockPos.ZERO);
		var center = piece(level, "bastion/treasure/bases/centers/center_0", new BlockPos(64, 0, 0));
		var wall = piece(level, "bastion/treasure/ramparts/top_wall", new BlockPos(128, 0, 0));
		var structure = new StructureStart(bastion, ChunkPos.ZERO, 0, new PiecesContainer(List.of(start, center, wall)));
		var selected = WitherSkullWorldgen.choose(level, structure, false);
		helper.assertTrue(selected.size() == 1 && center.getBoundingBox().isInside(selected.getFirst().pos()),
			"Treasure bastions must replace exactly one gold block in the central treasure room");
		helper.succeed();
	}

	@GameTest
	public void bridgeGoldIsExcludedAndTheLimitIsPerBastion(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		var bastion = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.BASTION_REMNANT).value();
		var start = piece(level, "bastion/bridge/starting_pieces/entrance_base", BlockPos.ZERO);
		var bridge = piece(level, "bastion/bridge/bridge_pieces/bridge", new BlockPos(64, 0, 0));
		var entrance = piece(level, "bastion/bridge/starting_pieces/entrance", new BlockPos(128, 0, 0));
		var pieces = new PiecesContainer(List.of(start, bridge, entrance));
		boolean reachedLimit = false;
		for (int x = 0; x < 64; x++) {
			var structure = new StructureStart(bastion, new ChunkPos(x, 0), 0, pieces);
			var selected = WitherSkullWorldgen.choose(level, structure, false);
			helper.assertTrue(selected.size() <= 2, "The skull limit applies across all pieces of the bastion");
			helper.assertTrue(selected.stream().noneMatch(skull -> bridge.getBoundingBox().isInside(skull.pos())),
				"The bridge's 16 gold blocks must stay intact");
			reachedLimit |= selected.size() == 2;
		}
		helper.assertTrue(reachedLimit, "The eligible gold blocks should be able to yield two skulls");
		helper.succeed();
	}

	@GameTest
	public void ancientCityRollSelectsAtMostOneSkeletonSkull(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		var city = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.ANCIENT_CITY).value();
		var barracks = piece(level, "ancient_city/structures/barracks", BlockPos.ZERO);
		var pillar = piece(level, "ancient_city/structures/medium_pillar_1", new BlockPos(64, 0, 0));
		var pieces = new PiecesContainer(List.of(barracks, pillar));
		int replacements = 0;
		for (int x = 0; x < 300; x++) {
			var structure = new StructureStart(city, new ChunkPos(x, 0), 0, pieces);
			var selected = WitherSkullWorldgen.choose(level, structure, true);
			helper.assertTrue(selected.size() <= 1, "Each city can replace at most one skeleton skull");
			helper.assertTrue(selected.stream().allMatch(skull -> skull.source() == Blocks.SKELETON_SKULL && skull.skull() == WitherSkulls.SCULK.skull()),
				"Ancient cities must replace skeleton skulls with the sculk variant");
			replacements += selected.size();
		}
		helper.assertTrue(replacements > 60 && replacements < 140, "About one third of the city rolls should succeed");
		helper.succeed();
	}

	private static StructurePiece piece(ServerLevel level, String template, BlockPos pos) {
		var manager = level.getStructureTemplateManager();
		var element = StructurePoolElement.single(template).apply(StructureTemplatePool.Projection.RIGID);
		return new PoolElementStructurePiece(manager, element, pos, 0, Rotation.NONE,
			element.getBoundingBox(manager, pos, Rotation.NONE), JigsawStructure.DEFAULT_LIQUID_SETTINGS);
	}

	private static void placeSkulls(GameTestHelper helper, Block left, Block center, Block right, int last) {
		helper.setBlock(2, 1, 2, Blocks.SOUL_SAND);
		List<Block> skulls = List.of(left, center, right);
		for (int index = 0; index < skulls.size(); index++) {
			helper.setBlock(index + 1, 2, 2, Blocks.SOUL_SAND);
			if (index != last) {
				helper.setBlock(index + 1, 3, 2, skulls.get(index));
			}
		}
		Block skull = skulls.get(last);
		BlockPos pos = new BlockPos(last + 1, 3, 2);
		helper.setBlock(pos, skull);
		helper.assertTrue(helper.getLevel().getBlockEntity(helper.absolutePos(pos), BlockEntityTypes.SKULL).isPresent(),
			"The placed skull must have a valid vanilla skull block entity");
		skull.setPlacedBy(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos), null, new ItemStack(skull));
	}
}
