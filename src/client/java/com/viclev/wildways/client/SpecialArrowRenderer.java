package com.viclev.wildways.client;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.projectile.arrow.Arrow;

/** Vanilla arrow geometry with one fixed texture for a Wildways arrow type. */
public class SpecialArrowRenderer<T extends Arrow> extends ArrowRenderer<T, ArrowRenderState> {
	private final Identifier texture;

	public SpecialArrowRenderer(EntityRendererProvider.Context context, Identifier texture) {
		super(context);
		this.texture = texture;
	}

	@Override
	protected Identifier getTextureLocation(ArrowRenderState state) {
		return this.texture;
	}

	@Override
	public ArrowRenderState createRenderState() {
		return new ArrowRenderState();
	}
}
