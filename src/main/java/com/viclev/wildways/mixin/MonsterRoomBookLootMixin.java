package com.viclev.wildways.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MonsterRoomFeature.class)
public class MonsterRoomBookLootMixin {
	@Unique private static final ThreadLocal<EntityType<?>> wildways$selectedMob = new ThreadLocal<>();

	@Inject(method = "place", at = @At("HEAD"))
	private void wildways$clearPreviousSpawner(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin,
		CallbackInfoReturnable<Boolean> cir) {
		wildways$selectedMob.remove();
	}

	@Inject(method = "randomEntityId", at = @At("RETURN"))
	private void wildways$rememberSpawner(RandomSource random,
		CallbackInfoReturnable<EntityType<?>> cir) {
		wildways$selectedMob.set(cir.getReturnValue());
	}

	@Inject(method = "place", at = @At("RETURN"))
	private void wildways$matchBooksToSpawner(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin,
		CallbackInfoReturnable<Boolean> cir) {
		EntityType<?> mob = wildways$selectedMob.get();
		wildways$selectedMob.remove();
		if (!cir.getReturnValue() || mob == null) {
			return;
		}
		String variant = mob == EntityTypes.ZOMBIE ? "zombie"
			: mob == EntityTypes.SPIDER ? "spider" : mob == EntityTypes.SKELETON ? "skeleton" : null;
		if (variant == null) {
			return;
		}
		ResourceKey<LootTable> loot = ResourceKey.create(Registries.LOOT_TABLE,
			Identifier.fromNamespaceAndPath("wildways", "chests/dungeon_" + variant));
		for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-5, -1, -5), origin.offset(5, 1, 5))) {
			if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest
				&& BuiltInLootTables.SIMPLE_DUNGEON.equals(chest.getLootTable())) {
				chest.setLootTable(loot);
			}
		}
	}
}
