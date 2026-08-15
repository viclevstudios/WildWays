package com.viclev.wildways.client;

import com.viclev.wildways.ModMenuTypes;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class WildwaysClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(ModMenuTypes.ENDERMITE_BOX, EndermiteBoxScreen::new);
	}
}
