package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.viclev.wildways.PlantBlocks;
import com.viclev.wildways.PlantGrowth;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
abstract class PlantHoeMixin {
	@WrapOperation(method = "useOn", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/world/item/Item;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"))
	private InteractionResult wildways$recordHoeTier(Item item, UseOnContext context, Operation<InteractionResult> original) {
		int tier = PlantGrowth.hoeTier(item);
		if (tier < 0) return original.call(item, context);

		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState before = level.getBlockState(pos);
		InteractionResult result = original.call(item, context);
		if (result.consumesAction()) {
			if (!level.isClientSide()) {
				BlockState tilled = level.getBlockState(pos);
				if (tilled.getBlock() != before.getBlock() && tilled.hasProperty(PlantGrowth.TILLAGE)) {
					level.setBlock(pos, tilled.setValue(PlantGrowth.TILLAGE, tier), Block.UPDATE_CLIENTS);
				}
			}
			return result;
		}
		if (context.getClickedFace() == Direction.DOWN) return result;

		BlockState tilled;
		if (before.is(PlantBlocks.COMPOST) && level.isEmptyBlock(pos.above())) {
			tilled = PlantBlocks.COMPOST_FARMLAND.defaultBlockState().setValue(PlantGrowth.TILLAGE, tier);
		} else if (before.hasProperty(PlantGrowth.TILLAGE) && before.getValue(PlantGrowth.TILLAGE) != tier) {
			tilled = before.setValue(PlantGrowth.TILLAGE, tier);
		} else {
			return result;
		}

		if (!level.isClientSide()) {
			Player player = context.getPlayer();
			level.setBlockAndUpdate(pos, tilled);
			level.playSound(null, pos, SoundEvents.HOE_TILL.value(), SoundSource.BLOCKS);
			level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, tilled));
			if (player != null) context.getItemInHand().hurtAndBreak(1, player, context.getHand());
		}
		return InteractionResult.SUCCESS;
	}
}
