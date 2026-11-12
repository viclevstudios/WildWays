package com.viclev.wildways.client.mixin;

import com.viclev.wildways.WitherSkulls;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.SkullBlock;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkullBlockRenderer.class)
public abstract class WitherSkullRendererMixin {
	@Inject(method = "createModel", at = @At("HEAD"), cancellable = true)
	private static void wildways$createModel(EntityModelSet modelSet, SkullBlock.Type type, CallbackInfoReturnable<SkullModelBase> cir) {
		if (type instanceof WitherSkulls.SkullType) {
			cir.setReturnValue(new SkullModel(modelSet.bakeLayer(ModelLayers.WITHER_SKELETON_SKULL)));
		}
	}

	@Inject(method = "getSkullRenderType", at = @At("HEAD"), cancellable = true)
	private static void wildways$useVariantTexture(SkullBlock.Type type, @Nullable Identifier texture, CallbackInfoReturnable<RenderType> cir) {
		if (texture == null && type instanceof WitherSkulls.SkullType skullType) {
			Identifier customTexture = skullType.id().withPath(path -> "textures/entity/skull/" + path + ".png");
			Identifier skin = Minecraft.getInstance().getResourceManager().getResource(customTexture).isPresent()
				? customTexture
				: Identifier.withDefaultNamespace("textures/entity/skeleton/wither_skeleton.png");
			cir.setReturnValue(RenderTypes.entityCutoutZOffset(skin));
		}
	}
}
