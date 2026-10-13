package com.viclev.wildways.mixin;

import com.viclev.wildways.XpProgression;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlintAndSteelItem.class)
public abstract class FlintAndSteelPortalXpMixin {
	@Unique private boolean wildways$portalAlreadyNearby;

	@Inject(method = "useOn", at = @At("HEAD"))
	private void wildways$rememberPortal(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
		this.wildways$portalAlreadyNearby = hasPortal(context);
	}

	@Inject(method = "useOn", at = @At("TAIL"))
	private void wildways$rewardPortal(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
		if (cir.getReturnValue().consumesAction() && !this.wildways$portalAlreadyNearby && hasPortal(context)) {
			XpProgression.awardNetherPortalOpening(context);
		}
	}

	@Unique
	private static boolean hasPortal(UseOnContext context) {
		if (!(context.getLevel() instanceof ServerLevel level)) return false;
		BlockPos center = context.getClickedPos();
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -1, -2), center.offset(2, 3, 2))) {
			if (level.getBlockState(pos).is(Blocks.NETHER_PORTAL)) return true;
		}
		return false;
	}
}
