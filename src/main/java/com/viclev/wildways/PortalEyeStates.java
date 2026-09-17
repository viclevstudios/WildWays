package com.viclev.wildways;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public final class PortalEyeStates {
	public static final IntegerProperty EYE_TYPE = IntegerProperty.create("wildways_eye_type", 0, 12);

	private PortalEyeStates() {
	}

	public static int typeFor(Item item) {
		int index = ModItems.PORTAL_EYES.indexOf(item);
		if (index < 0) {
			throw new IllegalArgumentException("Not a portal eye: " + item);
		}
		return index + 1;
	}
}
