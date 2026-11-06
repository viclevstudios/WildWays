package com.viclev.wildways.mixin;

import com.viclev.wildways.StructureMaps;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PatrollingMonster.class)
public class PatrolCaptainMixin {
	@Inject(method = "finalizeSpawn", at = @At("RETURN"))
	private void wildways$markPatrolCaptain(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
		SpawnGroupData group, CallbackInfoReturnable<SpawnGroupData> cir) {
		if (reason == EntitySpawnReason.PATROL && (Object)this instanceof Pillager pillager && pillager.isPatrolLeader()) {
			pillager.addTag(StructureMaps.PATROL_CAPTAIN_TAG);
		}
	}
}
