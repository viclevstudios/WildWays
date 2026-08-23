package com.viclev.wildways.mixin;

import com.viclev.wildways.ModEffects;
import com.viclev.wildways.UneaseBlockBreakHandler;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerExplosion.class)
public class ServerExplosionMixin {
	@Inject(method = "interactWithBlocks", at = @At("HEAD"))
	private void wildways$spawnEndermitesForUneasyCreeper(List<BlockPos> positions, CallbackInfo ci) {
		ServerExplosion explosion = (ServerExplosion) (Object) this;
		if (!(explosion.getDirectSourceEntity() instanceof Creeper creeper)) {
			return;
		}

		MobEffectInstance unease = creeper.getEffect(ModEffects.UNEASE);
		if (unease == null) {
			return;
		}

		ServerLevel level = explosion.level();
		float spawnChance = UneaseBlockBreakHandler.getSpawnChance(unease);
		for (BlockPos pos : positions) {
			if (!level.getBlockState(pos).isSolid()) {
				continue;
			}

			if (level.getRandom().nextFloat() < spawnChance) {
				UneaseBlockBreakHandler.spawnEndermite(level, pos);
			}
		}
	}
}
