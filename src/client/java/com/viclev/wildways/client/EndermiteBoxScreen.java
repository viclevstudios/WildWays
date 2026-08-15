package com.viclev.wildways.client;

import com.viclev.wildways.EndermiteBoxMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class EndermiteBoxScreen extends AbstractContainerScreen<EndermiteBoxMenu> {
	private static final Identifier SHULKER_BOX_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/shulker_box.png");
	private static final Identifier GENERIC_CONTAINER_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
	private static final int TEXTURE_SIZE = 256;
	private static final int SLOT_SIZE = 18;
	private static final int SLOT_COLUMNS = 4;
	private static final int SLOT_ROWS = 3;
	private static final int CONTAINER_START_X = 52;
	private static final int CONTAINER_START_Y = 18;
	private static final int VANILLA_SLOT_TEXTURE_X = 7;
	private static final int VANILLA_SLOT_TEXTURE_Y = 17;
	private static final int PANEL_COLOR = 0xFFC6C6C6;

	public EndermiteBoxScreen(EndermiteBoxMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 176, 167);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractBackground(graphics, mouseX, mouseY, delta);
		graphics.blit(RenderPipelines.GUI_TEXTURED, SHULKER_BOX_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, TEXTURE_SIZE, TEXTURE_SIZE);
		graphics.fill(this.leftPos + VANILLA_SLOT_TEXTURE_X, this.topPos + VANILLA_SLOT_TEXTURE_Y, this.leftPos + this.imageWidth - VANILLA_SLOT_TEXTURE_X, this.topPos + CONTAINER_START_Y + SLOT_ROWS * SLOT_SIZE - 1, PANEL_COLOR);

		for (int y = 0; y < SLOT_ROWS; y++) {
			for (int x = 0; x < SLOT_COLUMNS; x++) {
				int slotX = this.leftPos + CONTAINER_START_X - 1 + x * SLOT_SIZE;
				int slotY = this.topPos + CONTAINER_START_Y - 1 + y * SLOT_SIZE;
				graphics.blit(RenderPipelines.GUI_TEXTURED, GENERIC_CONTAINER_TEXTURE, slotX, slotY, VANILLA_SLOT_TEXTURE_X, VANILLA_SLOT_TEXTURE_Y, SLOT_SIZE, SLOT_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
			}
		}
	}
}
