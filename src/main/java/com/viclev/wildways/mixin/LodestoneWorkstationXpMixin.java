package com.viclev.wildways.mixin;

import com.viclev.wildways.XpProgression;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CompassItem.class)
public abstract class LodestoneWorkstationXpMixin {
	@Inject(method = "useOn", at = @At("TAIL"))
	private void wildways$rewardLodestoneBinding(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
		if (context.getPlayer() instanceof ServerPlayer player && cir.getReturnValue() == InteractionResult.SUCCESS
			&& context.getLevel().getBlockState(context.getClickedPos()).is(Blocks.LODESTONE)) {
			XpProgression.awardWorkstationOnce(player, Blocks.LODESTONE);
		}
	}
}
