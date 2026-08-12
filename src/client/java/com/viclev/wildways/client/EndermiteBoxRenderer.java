package com.viclev.wildways.client;

import com.viclev.wildways.EndermiteBoxBlock;
import com.viclev.wildways.EndermiteBoxBlockEntity;
import com.viclev.wildways.Wildways;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;
import net.minecraft.client.renderer.blockentity.state.ShulkerBoxRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class EndermiteBoxRenderer implements BlockEntityRenderer<EndermiteBoxBlockEntity, ShulkerBoxRenderState> {
	private static final SpriteId TEXTURE = Sheets.SHULKER_MAPPER.apply(Wildways.id("endermite_box"));

	private final ShulkerBoxRenderer shulkerRenderer;

	public EndermiteBoxRenderer(BlockEntityRendererProvider.Context context) {
		this.shulkerRenderer = new ShulkerBoxRenderer(context);
	}

	@Override
	public ShulkerBoxRenderState createRenderState() {
		return new ShulkerBoxRenderState();
	}

	@Override
	public void extractRenderState(EndermiteBoxBlockEntity endermiteBox, ShulkerBoxRenderState state, float tickDelta, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(endermiteBox, state, tickDelta, cameraPos, breakProgress);
		state.direction = endermiteBox.getBlockState().getValueOrElse(EndermiteBoxBlock.FACING, Direction.UP);
		state.progress = endermiteBox.getProgress(tickDelta);
	}

	@Override
	public void submit(ShulkerBoxRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
		matrices.pushPose();
		matrices.mulPose(ShulkerBoxRenderer.modelTransform(state.direction));
		this.shulkerRenderer.submit(
			matrices,
			queue,
			state.lightCoords,
			OverlayTexture.NO_OVERLAY,
			state.progress,
			state.breakProgress,
			TEXTURE,
			0
		);
		matrices.popPose();
	}
}
