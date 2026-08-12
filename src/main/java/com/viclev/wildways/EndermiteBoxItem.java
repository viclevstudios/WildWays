package com.viclev.wildways;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class EndermiteBoxItem extends BlockItem {
	public EndermiteBoxItem(Block block, Item.Properties properties) {
		super(block, properties);
	}

	@Override
	public boolean canFitInsideContainerItems() {
		return false;
	}
}
