package com.viclev.wildways.client;

import com.viclev.wildways.ModMenuTypes;
import com.viclev.wildways.ModEntityTypes;
import com.viclev.wildways.Wildways;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class WildwaysClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(ModMenuTypes.ENDERMITE_BOX, EndermiteBoxScreen::new);
		MenuScreens.register(ModMenuTypes.FLETCHING_TABLE, FletchingTableScreen::new);
		EntityRenderers.register(ModEntityTypes.TURTLE_ARROW, context -> new SpecialArrowRenderer<>(context, Wildways.id("textures/entity/projectiles/turtle_arrow.png")));
		EntityRenderers.register(ModEntityTypes.RANGE_ARROW, context -> new SpecialArrowRenderer<>(context, Wildways.id("textures/entity/projectiles/range_arrow.png")));
		EntityRenderers.register(ModEntityTypes.EXPLOSIVE_ARROW, context -> new SpecialArrowRenderer<>(context, Wildways.id("textures/entity/projectiles/explosive_arrow.png")));
		ClientTickEvents.END_CLIENT_TICK.register(HeldItemInfoHud::updateActionBar);
	}
}
