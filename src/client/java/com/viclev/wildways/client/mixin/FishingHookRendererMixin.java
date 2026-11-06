package com.viclev.wildways.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.viclev.wildways.FishingBait;
import com.viclev.wildways.FishingMinigame;
import com.viclev.wildways.Wildways;
import java.util.EnumMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHookRenderer.class)
public abstract class FishingHookRendererMixin {
	@Unique
	private static final RenderStateDataKey<RenderType> WILDWAYS_BOBBER = RenderStateDataKey.create(() -> "Wildways bobber");
	@Unique
	private static final Map<FishingBait.Type, RenderType> WILDWAYS_BOBBER_TEXTURES = new EnumMap<>(FishingBait.Type.class);

	static {
		for (FishingBait.Type bait : FishingBait.Type.values()) {
			WILDWAYS_BOBBER_TEXTURES.put(bait, RenderTypes.entityCutoutCull(
				Wildways.id("textures/item/fishing/fishing_hook_" + bait.getSerializedName() + ".png")
			));
		}
	}

	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/projectile/FishingHook;Lnet/minecraft/client/renderer/entity/state/FishingHookRenderState;F)V",
		at = @At("TAIL"))
	private void wildways$extractBobberTexture(FishingHook hook, FishingHookRenderState state, float partialTicks, CallbackInfo ci) {
		state.setData(WILDWAYS_BOBBER, WILDWAYS_BOBBER_TEXTURES.get(hook.getAttached(FishingMinigame.CAST_BAIT)));
	}

	@ModifyExpressionValue(method = "submit", at = @At(value = "FIELD",
		target = "Lnet/minecraft/client/renderer/entity/FishingHookRenderer;RENDER_TYPE:Lnet/minecraft/client/renderer/rendertype/RenderType;"))
	private RenderType wildways$chooseBobberTexture(RenderType vanilla, FishingHookRenderState state) {
		RenderType baitTexture = state.getData(WILDWAYS_BOBBER);
		return baitTexture == null ? vanilla : baitTexture;
	}
}
