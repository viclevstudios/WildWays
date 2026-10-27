package com.viclev.wildways.mixin;

import com.viclev.wildways.XpProgression;
import com.viclev.wildways.BeaconActivationState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconFirstActivationXpMixin implements BeaconActivationState {
	@Unique private boolean wildways$firstActivationHandled;

	@Override
	@Unique
	public boolean wildways$wasFirstActivationRewarded() {
		return this.wildways$firstActivationHandled;
	}

	@Override
	@Unique
	public void wildways$setFirstActivationRewarded() {
		this.wildways$firstActivationHandled = true;
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private static void wildways$rewardFirstActivation(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos,
		net.minecraft.world.level.block.state.BlockState state, BeaconBlockEntity beacon, CallbackInfo ci) {
		BeaconActivationState activationState = (BeaconActivationState)(Object)beacon;
		if (!activationState.wildways$wasFirstActivationRewarded() && beacon.getBeamSections().size() > 0
			&& level instanceof ServerLevel serverLevel) {
			if (XpProgression.awardFirstBeaconActivation(serverLevel, beacon.getBlockPos())) {
				activationState.wildways$setFirstActivationRewarded();
			}
		}
	}
}

