package com.viclev.wildways.mixin;

import net.minecraft.world.entity.monster.Endermite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Endermite.class)
public class EndermiteMixin {
	@ModifyConstant(method = "aiStep", constant = @Constant(intValue = 2400))
	private int wildways$removeFixedDespawnTimer(int lifetime) {
		return Integer.MAX_VALUE;
	}
}
