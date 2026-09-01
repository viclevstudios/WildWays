package com.viclev.wildways.client;

import com.viclev.wildways.FletchingTableMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** A furnace-style work area: three material slots, an arrow and a result slot. */
public class FletchingTableScreen extends AbstractContainerScreen<FletchingTableMenu> {
	private static final Identifier FURNACE_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/furnace.png");
	private static final Identifier GENERIC_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
	private static final int PANEL_COLOR = 0xFFC6C6C6;

	public FletchingTableScreen(FletchingTableMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 176, 166);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractBackground(graphics, mouseX, mouseY, delta);
		graphics.blit(RenderPipelines.GUI_TEXTURED, FURNACE_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

		// The furnace artwork provides a clean vanilla frame and inventory. Its two
		// input slots are covered before drawing this menu's one-row recipe layout.
		graphics.fill(this.leftPos + 8, this.topPos + 17, this.leftPos + 168, this.topPos + 77, PANEL_COLOR);
		for (int index = 0; index < 3; index++) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, GENERIC_TEXTURE, this.leftPos + 25 + index * 18, this.topPos + 34, 7, 17, 18, 18, 256, 256);
		}
		graphics.blit(RenderPipelines.GUI_TEXTURED, GENERIC_TEXTURE, this.leftPos + 115, this.topPos + 34, 7, 17, 18, 18, 256, 256);
		// Exact vanilla furnace arrow, without scaling or clipping another texture.
		graphics.blit(RenderPipelines.GUI_TEXTURED, FURNACE_TEXTURE, this.leftPos + 82, this.topPos + 35, 79.0F, 34.0F, 24, 17, 256, 256);
	}
}
