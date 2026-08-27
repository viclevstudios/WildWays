package com.viclev.wildways.mixin;

import com.viclev.wildways.QuarantineFoundationProcessor;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructureTemplate.class)
public abstract class StructureTemplateEntityOffsetMixin {
	@Unique
	private static final ThreadLocal<Deque<StructurePlaceSettings>> WILDWAYS_ACTIVE_SETTINGS = ThreadLocal.withInitial(ArrayDeque::new);

	@Inject(method = "placeInWorld", at = @At("HEAD"))
	private void wildways$trackPlaceSettings(
		ServerLevelAccessor level,
		BlockPos origin,
		BlockPos referencePos,
		StructurePlaceSettings settings,
		RandomSource random,
		int flags,
		CallbackInfoReturnable<Boolean> cir
	) {
		WILDWAYS_ACTIVE_SETTINGS.get().push(settings);
	}

	@Inject(method = "placeInWorld", at = @At("RETURN"))
	private void wildways$clearPlaceSettings(
		ServerLevelAccessor level,
		BlockPos origin,
		BlockPos referencePos,
		StructurePlaceSettings settings,
		RandomSource random,
		int flags,
		CallbackInfoReturnable<Boolean> cir
	) {
		Deque<StructurePlaceSettings> settingsStack = WILDWAYS_ACTIVE_SETTINGS.get();
		settingsStack.pop();
		if (settingsStack.isEmpty()) {
			WILDWAYS_ACTIVE_SETTINGS.remove();
		}
	}

	@Redirect(
		method = "placeInWorld",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;placeEntities(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Mirror;Lnet/minecraft/world/level/block/Rotation;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/BoundingBox;ZLnet/minecraft/util/ProblemReporter;)V"
		)
	)
	private void wildways$moveEntitiesWithTheirTemplate(
		StructureTemplate template,
		ServerLevelAccessor level,
		BlockPos origin,
		Mirror mirror,
		Rotation rotation,
		BlockPos rotationPivot,
		BoundingBox boundingBox,
		boolean finalizeEntities,
		ProblemReporter problemReporter
	) {
		int offsetY = QuarantineFoundationProcessor.takeEntityOffset(WILDWAYS_ACTIVE_SETTINGS.get().peek());
		((StructureTemplateEntityInvoker) template).wildways$placeEntities(
			level,
			origin.offset(0, offsetY, 0),
			mirror,
			rotation,
			rotationPivot,
			offsetY == 0 ? boundingBox : boundingBox.moved(0, offsetY, 0),
			finalizeEntities,
			problemReporter
		);
	}
}
