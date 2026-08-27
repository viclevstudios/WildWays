package com.viclev.wildways.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StructureTemplate.class)
public interface StructureTemplateEntityInvoker {
	@Invoker("placeEntities")
	void wildways$placeEntities(
		ServerLevelAccessor level,
		BlockPos origin,
		Mirror mirror,
		Rotation rotation,
		BlockPos rotationPivot,
		BoundingBox boundingBox,
		boolean finalizeEntities,
		ProblemReporter problemReporter
	);
}
