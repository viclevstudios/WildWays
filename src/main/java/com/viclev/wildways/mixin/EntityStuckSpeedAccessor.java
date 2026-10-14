package com.viclev.wildways.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityStuckSpeedAccessor {
	@Accessor("stuckSpeedMultiplier")
	void wildways$setStuckSpeedMultiplier(Vec3 multiplier);
}
