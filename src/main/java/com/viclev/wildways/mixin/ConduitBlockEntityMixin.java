package com.viclev.wildways.mixin;

import com.viclev.wildways.EyeTransformations;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ConduitBlockEntity.class)
public class ConduitBlockEntityMixin {
	@Inject(method = "applyEffects", at = @At("TAIL"))
	private static void wildways$transformEnderEyes(Level level, BlockPos conduitPos, List<BlockPos> effectBlocks, CallbackInfo ci) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}
		int range = effectBlocks.size() / 7 * 16;
		AABB area = new AABB(conduitPos).inflate(range);
		for (ItemEntity itemEntity : serverLevel.getEntitiesOfClass(ItemEntity.class, area, item -> item.getItem().is(Items.ENDER_EYE))) {
			if (conduitPos.closerThan(itemEntity.blockPosition(), range) && itemEntity.isInWaterOrRain()) {
				EyeTransformations.transformWithConduit(serverLevel, itemEntity);
			}
		}
	}
}
