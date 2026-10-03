package com.viclev.wildways.client;

import com.viclev.wildways.EnchantingRules;
import com.viclev.wildways.Wildways;
import com.viclev.wildways.WildwaysEnchantingMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.locale.Language;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.enchantment.Enchantment;

public class WildwaysEnchantingScreen extends AbstractContainerScreen<WildwaysEnchantingMenu> {
	private static final Identifier INVENTORY_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
	private static final Identifier FURNACE_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/furnace.png");
	private static final Identifier[] PLACEHOLDERS = {
		Wildways.id("textures/gui/enchanting/item_placeholder.png"),
		Wildways.id("textures/gui/enchanting/lapis_placeholder.png"),
		Wildways.id("textures/gui/enchanting/book_placeholder.png"),
		Wildways.id("textures/gui/enchanting/rune_placeholder.png"),
		Wildways.id("textures/gui/enchanting/catalyst_placeholder.png")
	};
	private static final int TEXT_COLOR = 0xFF404040;
	private static final int PANEL_COLOR = 0xFFC6C6C6;
	private Button previous;
	private Button next;

	public WildwaysEnchantingScreen(WildwaysEnchantingMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 176, 200);
		this.inventoryLabelY = 103;
	}

	@Override
	protected void init() {
		super.init();
		this.previous = this.addRenderableWidget(Button.builder(Component.literal("<"), button -> this.select(0))
			.bounds(this.leftPos + 138, this.topPos + 27, 14, 16).build());
		this.next = this.addRenderableWidget(Button.builder(Component.literal(">"), button -> this.select(1))
			.bounds(this.leftPos + 154, this.topPos + 27, 14, 16).build());
	}

	private void select(int direction) {
		this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, direction);
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		this.previous.active = this.next.active = this.menu.recipes().size() > 1;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractBackground(graphics, mouseX, mouseY, delta);
		int x = this.leftPos;
		int y = this.topPos;
		// Reuse the vanilla chest frame and inventory, keeping its slot bevels and gray palette.
		graphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY_TEXTURE, x, y, 0, 0, 176, 104, 256, 256);
		graphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY_TEXTURE, x, y + 104, 0, 126, 176, 96, 256, 256);
		graphics.fill(x + 7, y + 16, x + 169, y + 115, PANEL_COLOR);
		for (int index = 0; index < 5; index++) {
			this.drawSlot(graphics, x + 8 + index * 22, y + 70);
			if (!this.menu.getSlot(index).hasItem()) {
				graphics.blit(RenderPipelines.GUI_TEXTURED, PLACEHOLDERS[index], x + 8 + index * 22, y + 70,
					0, 0, 16, 16, 16, 16);
			}
		}
		this.drawSlot(graphics, x + 150, y + 70);
		graphics.blit(RenderPipelines.GUI_TEXTURED, FURNACE_TEXTURE, x + 120, y + 69,
			79, 34, 24, 17, 256, 256);
	}

	private void drawSlot(GuiGraphicsExtractor graphics, int x, int y) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY_TEXTURE, x - 1, y - 1,
			7, 17, 18, 18, 256, 256);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		graphics.text(this.font, this.title, 8, 9, TEXT_COLOR, false);
		var enchantment = this.menu.selectedEnchantment();
		if (enchantment != null) {
			int level = EnchantingRules.enchantmentLevel(enchantment, this.menu.runeTier());
			Component name = level > 0 ? Enchantment.getFullname(enchantment, level) : enchantment.value().description();
			graphics.text(this.font, Language.getInstance().getVisualOrder(this.font.substrByWidth(name, 122)), 8, 29, TEXT_COLOR, false);
		}
		if (this.menu.runeTier() == 0) {
			graphics.text(this.font, Component.translatable("container.wildways.enchanting_nearby_shelves", this.menu.shelfCount()), 8, 44, TEXT_COLOR, false);
		} else {
			graphics.text(this.font, Component.translatable("container.wildways.enchanting_requirements",
				EnchantingRules.experienceCost(this.menu.runeTier(), this.menu.hasCatalyst()),
				this.menu.shelfCount(), EnchantingRules.requiredShelves(this.menu.runeTier())), 8, 44, TEXT_COLOR, false);
		}
		if (this.menu.status() != 0) {
			graphics.text(this.font, Language.getInstance().getVisualOrder(this.font.substrByWidth(Component.translatable("container.wildways.enchanting_status_" + this.menu.status()), 160)),
				8, 55, this.menu.status() == 5 ? 0xFF316038 : 0xFF9C3535, false);
		}
		graphics.text(this.font, this.playerInventoryTitle, 8, 103, TEXT_COLOR, false);
	}
}
