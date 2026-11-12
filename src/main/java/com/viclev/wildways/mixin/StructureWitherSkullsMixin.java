package com.viclev.wildways.mixin;

import com.viclev.wildways.WitherSkullWorldgen;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureStart.class)
public abstract class StructureWitherSkullsMixin {
	@Unique
	private volatile @Nullable List<WitherSkullWorldgen.Replacement> wildways$witherSkullReplacements;

	@Inject(method = "placeInChunk", at = @At("TAIL"))
	private void wildways$placeWitherSkulls(
		WorldGenLevel level, StructureManager structures, ChunkGenerator generator, RandomSource random,
		BoundingBox chunkBounds, ChunkPos chunk, CallbackInfo ci
	) {
		StructureStart start = (StructureStart) (Object) this;
		if (!start.isValid()) {
			return;
		}
		List<WitherSkullWorldgen.Replacement> replacements = wildways$witherSkullReplacements;
		if (replacements == null) {
			var structure = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).wrapAsHolder(start.getStructure());
			boolean ancientCity = structure.is(BuiltinStructures.ANCIENT_CITY);
			replacements = ancientCity || structure.is(BuiltinStructures.BASTION_REMNANT)
				? WitherSkullWorldgen.choose(level, start, ancientCity)
				: List.of();
			wildways$witherSkullReplacements = replacements;
		}
		for (var replacement : replacements) {
			replacement.place(level, chunkBounds);
		}
	}
}
