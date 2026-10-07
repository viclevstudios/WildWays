package com.viclev.wildways.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.viclev.wildways.WorkstationInventory;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EnchantingItemsRenderer extends EnchantTableRenderer {
	private final ItemModelResolver itemModelResolver;

	public EnchantingItemsRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
		this.itemModelResolver = context.itemModelResolver();
	}

	@Override public EnchantingItemsRenderState createRenderState() { return new EnchantingItemsRenderState(); }

	@Override
	public void extractRenderState(EnchantingTableBlockEntity entity, EnchantTableRenderState renderState,
		float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		super.extractRenderState(entity, renderState, partialTicks, cameraPosition, breakProgress);
		EnchantingItemsRenderState state = (EnchantingItemsRenderState)renderState;
		WorkstationInventory inventory = (WorkstationInventory)entity;
		for (int i = 0; i < state.items.length; i++) {
			this.itemModelResolver.updateForTopItem(state.items[i], inventory.getItem(i), ItemDisplayContext.FIXED,
				entity.getLevel(), null, (int)entity.getBlockPos().asLong() + i);
		}
	}

	@Override
	public void submit(EnchantTableRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		super.submit(renderState, poseStack, collector, camera);
		EnchantingItemsRenderState state = (EnchantingItemsRenderState)renderState;
		if (!state.items[0].isEmpty()) {
			poseStack.pushPose();
			poseStack.translate(0.5F, 1.45F + (float)Math.sin(state.time * 0.09F) * 0.05F, 0.5F);
			poseStack.mulPose(Axis.YP.rotation(state.time * 0.035F));
			poseStack.scale(0.75F, 0.75F, 0.75F);
			state.items[0].submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}
		for (int i = 1; i < state.items.length && !state.items[0].isEmpty(); i++) {
			if (state.items[i].isEmpty()) continue;
			float angle = state.time * 0.025F + (i - 1) * (float)Math.PI / 2.0F;
			poseStack.pushPose();
			poseStack.translate(0.5F + (float)Math.cos(angle) * 0.45F,
				1.25F + (float)Math.sin(state.time * 0.08F + i) * 0.03F,
				0.5F + (float)Math.sin(angle) * 0.45F);
			poseStack.mulPose(Axis.YP.rotation(-angle));
			poseStack.scale(0.37F, 0.37F, 0.37F);
			state.items[i].submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}
	}
}
