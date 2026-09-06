package com.viclev.wildways.mixin;

import net.minecraft.world.level.levelgen.structure.structures.StrongholdPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(StrongholdPieces.PortalRoom.class)
public class StrongholdPortalRoomMixin {
	@ModifyConstant(method = "postProcess", constant = @Constant(floatValue = 0.9F))
	private float wildways$neverGenerateFilledPortalFrames(float original) {
		return Float.POSITIVE_INFINITY;
	}
}
