package com.viclev.wildways.mixin;

import com.viclev.wildways.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
		int range = effectBlocks.size() / 7 * 16;
		AABB area = new AABB(conduitPos).inflate(range);
		for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, area, item -> item.getItem().is(Items.ENDER_EYE))) {
			if (conduitPos.closerThan(itemEntity.blockPosition(), range) && itemEntity.isInWaterOrRain()) {
				itemEntity.setItem(itemEntity.getItem().transmuteCopy(ModItems.EYE_OF_WATER));
				level.playSound(null, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), SoundEvents.CONDUIT_ACTIVATE, SoundSource.BLOCKS, 0.8F, 1.35F);
			}
		}
	}
}
