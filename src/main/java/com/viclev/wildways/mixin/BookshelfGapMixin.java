package com.viclev.wildways.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantingTableBlock.class)
public class BookshelfGapMixin {
	@Inject(method = "isValidBookShelf", at = @At("HEAD"), cancellable = true)
	private static void wildways$allowPartialGapBlocks(Level level, BlockPos pos, BlockPos offset, CallbackInfoReturnable<Boolean> cir) {
		BlockPos gap = pos.offset(offset.getX() / 2, offset.getY(), offset.getZ() / 2);
		cir.setReturnValue(level.getBlockState(pos.offset(offset)).is(BlockTags.ENCHANTMENT_POWER_PROVIDER)
			&& !level.getBlockState(gap).isCollisionShapeFullBlock(level, gap));
	}
}
