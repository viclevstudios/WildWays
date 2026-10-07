package com.viclev.wildways.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

public class AnvilItemsRenderState extends BlockEntityRenderState {
	public Direction facing = Direction.NORTH;
	public final ItemStackRenderState[] items = {new ItemStackRenderState(), new ItemStackRenderState()};
}
