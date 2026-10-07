package com.viclev.wildways.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentPieces;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Two supply chests flank the entry room without blocking its centre aisle. */
@Mixin(OceanMonumentPieces.OceanMonumentEntryRoom.class)
public abstract class OceanMonumentBookChestsMixin extends StructurePiece {
	private static final ResourceKey<LootTable> WILDWAYS$SUPPLY_LOOT = ResourceKey.create(
		Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("wildways", "chests/monument_supply"));

	protected OceanMonumentBookChestsMixin() {
		super(null, 0, new BoundingBox(0, 0, 0, 0, 0, 0));
	}

	@Inject(method = "postProcess", at = @At("TAIL"))
	private void wildways$addSupplyChests(WorldGenLevel world, StructureManager manager,
		ChunkGenerator generator, RandomSource random, BoundingBox bounds, ChunkPos chunk, BlockPos pivot,
		CallbackInfo ci) {
		createChest(world, bounds, random, 1, 1, 5, WILDWAYS$SUPPLY_LOOT);
		createChest(world, bounds, random, 6, 1, 5, WILDWAYS$SUPPLY_LOOT);
	}
}
