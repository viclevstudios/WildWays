package com.viclev.wildways.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.viclev.wildways.AnvilStorageBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class AnvilItemsRenderer implements BlockEntityRenderer<AnvilStorageBlockEntity, AnvilItemsRenderState> {
	private final ItemModelResolver itemModelResolver;

	public AnvilItemsRenderer(BlockEntityRendererProvider.Context context) {
		this.itemModelResolver = context.itemModelResolver();
	}

	@Override public AnvilItemsRenderState createRenderState() { return new AnvilItemsRenderState(); }

	@Override
	public void extractRenderState(AnvilStorageBlockEntity entity, AnvilItemsRenderState state, float partialTicks,
		Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
		state.facing = entity.getBlockState().getValue(AnvilBlock.FACING);
		for (int i = 0; i < state.items.length; i++) {
			this.itemModelResolver.updateForTopItem(state.items[i], entity.getItem(i), ItemDisplayContext.FIXED,
				entity.getLevel(), null, (int)entity.getBlockPos().asLong() + i);
		}
	}

	@Override
	public void submit(AnvilItemsRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		for (int i = 0; i < state.items.length; i++) {
			if (state.items[i].isEmpty()) continue;
			poseStack.pushPose();
			poseStack.translate(0.5F, 0, 0.5F);
			poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));
			poseStack.translate(0, 1.03F, i == 0 ? -0.25F : 0.25F);
			poseStack.mulPose(Axis.XP.rotationDegrees(90));
			poseStack.scale(0.52F, 0.52F, 0.52F);
			state.items[i].submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}
	}
}
