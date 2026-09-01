package com.viclev.wildways.client;

import com.viclev.wildways.ModMenuTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screens.MenuScreens;

public class WildwaysClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(ModMenuTypes.ENDERMITE_BOX, EndermiteBoxScreen::new);
		MenuScreens.register(ModMenuTypes.FLETCHING_TABLE, FletchingTableScreen::new);
		ClientTickEvents.END_CLIENT_TICK.register(HeldItemInfoHud::updateActionBar);
	}
}
