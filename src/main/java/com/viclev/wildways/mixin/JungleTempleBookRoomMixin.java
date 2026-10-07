package com.viclev.wildways.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.structures.JungleTemplePiece;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A small alcove off the temple's lower corridor, with its own book chest. */
@Mixin(JungleTemplePiece.class)
public abstract class JungleTempleBookRoomMixin extends StructurePiece {
	private static final ResourceKey<LootTable> WILDWAYS$ROOM_LOOT = ResourceKey.create(
		Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("wildways", "chests/jungle_hidden_room"));

	protected JungleTempleBookRoomMixin() {
		super(null, 0, new BoundingBox(0, 0, 0, 0, 0, 0));
	}

	@Inject(method = "postProcess", at = @At("TAIL"))
	private void wildways$addBookRoom(WorldGenLevel world, StructureManager manager,
		ChunkGenerator generator, RandomSource random, BoundingBox bounds, ChunkPos chunk, BlockPos pivot,
		CallbackInfo ci) {
		// The lower corridor runs along x=1..3. Open the pocket beside the
		// stairwell; z=9 contains the final stair and must remain untouched.
		generateAirBox(world, bounds, 3, -3, 10, 7, -1, 11);
		placeBlock(world, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 6, -4, 11, bounds);
		createChest(world, bounds, random, 6, -3, 11, WILDWAYS$ROOM_LOOT);
	}
}
