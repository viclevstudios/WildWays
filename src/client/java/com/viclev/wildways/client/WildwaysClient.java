package com.viclev.wildways.client;

import com.viclev.wildways.ModMenuTypes;
import com.viclev.wildways.ModBlockEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;

public class WildwaysClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(ModMenuTypes.ENDERMITE_BOX, EndermiteBoxScreen::new);
		BlockEntityRendererRegistry.register(ModBlockEntities.ENDERMITE_BOX, EndermiteBoxRenderer::new);
	}
}
