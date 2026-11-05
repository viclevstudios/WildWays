package com.viclev.wildways.client;

import com.viclev.wildways.FishingBait;
import com.viclev.wildways.ModMenuTypes;
import com.viclev.wildways.ModEntityTypes;
import com.viclev.wildways.ModBlockEntities;
import com.viclev.wildways.Wildways;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityTypes;

public class WildwaysClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(ModMenuTypes.ENCHANTING, WildwaysEnchantingScreen::new);
		MenuScreens.register(ModMenuTypes.ENDERMITE_BOX, EndermiteBoxScreen::new);
		MenuScreens.register(ModMenuTypes.FLETCHING_TABLE, FletchingTableScreen::new);
		BlockEntityRenderers.register(BlockEntityTypes.ENCHANTING_TABLE, EnchantingItemsRenderer::new);
		BlockEntityRenderers.register(ModBlockEntities.ANVIL_STORAGE, AnvilItemsRenderer::new);
		EntityRenderers.register(ModEntityTypes.TURTLE_ARROW, context -> new SpecialArrowRenderer<>(context, Wildways.id("textures/entity/projectiles/turtle_arrow.png")));
		EntityRenderers.register(ModEntityTypes.RANGE_ARROW, context -> new SpecialArrowRenderer<>(context, Wildways.id("textures/entity/projectiles/range_arrow.png")));
		EntityRenderers.register(ModEntityTypes.EXPLOSIVE_ARROW, context -> new SpecialArrowRenderer<>(context, Wildways.id("textures/entity/projectiles/explosive_arrow.png")));
		ClientTickEvents.END_CLIENT_TICK.register(HeldItemInfoHud::updateActionBar);
		FishingScreen.initialize();
		ItemTooltipCallback.EVENT.register((stack, context, flags, lines) -> {
			FishingBait bait = stack.get(FishingBait.LOADED_BAIT);
			if (bait != null) {
				lines.add(Component.translatable("wildways.fishing.bait.loaded", bait.type().nameComponent(),
					bait.charges(), FishingBait.MAX_CHARGES).withStyle(ChatFormatting.GRAY));
			}
			if (stack.has(FishingBait.RICH_FISH)) {
				lines.add(Component.translatable("wildways.fishing.rich_fish").withStyle(ChatFormatting.GRAY));
			}
		});
	}
}
