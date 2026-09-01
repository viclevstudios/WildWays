package com.viclev.wildways.client.mixin;

import com.viclev.wildways.ModEffects;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Lets the client lightmap treat Supreme Night Vision as night vision. */
@Mixin(LightmapRenderStateExtractor.class)
public class SupremeNightVisionLightmapMixin {
	@Redirect(
		method = "extract",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/core/Holder;)Z", ordinal = 0)
	)
	private boolean wildways$hasNightVision(LocalPlayer player, Holder<MobEffect> effect) {
		return player.hasEffect(effect)
			|| (effect.equals(MobEffects.NIGHT_VISION) && player.hasEffect(ModEffects.SUPREME_NIGHT_VISION));
	}
}
