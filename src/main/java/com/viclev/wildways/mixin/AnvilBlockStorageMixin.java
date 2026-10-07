package com.viclev.wildways.mixin;

import com.viclev.wildways.AnvilStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AnvilBlock.class)
public class AnvilBlockStorageMixin implements EntityBlock {
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new AnvilStorageBlockEntity(pos, state);
	}
}
