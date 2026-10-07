package com.viclev.wildways.client;

import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class EnchantingItemsRenderState extends EnchantTableRenderState {
	public final ItemStackRenderState[] items = new ItemStackRenderState[5];

	public EnchantingItemsRenderState() {
		for (int i = 0; i < this.items.length; i++) this.items[i] = new ItemStackRenderState();
	}
}
