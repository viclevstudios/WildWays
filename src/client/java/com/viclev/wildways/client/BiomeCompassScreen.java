package com.viclev.wildways.client;

import com.viclev.wildways.BiomeCompassItem;
import com.viclev.wildways.BiomeCompassMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class BiomeCompassScreen extends AbstractContainerScreen<BiomeCompassMenu> {
	private static final Component INSTRUCTION = Component.translatable("wildways.biome_compass.instruction");
	private static final Component MISSING_BIOME = Component.translatable("wildways.biome_compass.missing_biome");
	private BiomeList list;
	private Button confirm;

	public BiomeCompassScreen(BiomeCompassMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 0, 0);
	}

	@Override
	protected void init() {
		super.init();
		this.list = this.addRenderableWidget(new BiomeList());
		this.confirm = this.addRenderableWidget(Button.builder(Component.translatable("wildways.biome_compass.bind"), button -> {
			BiomeEntry entry = this.list.getSelected();
			if (entry != null && this.minecraft.gameMode != null) {
				this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, entry.index);
			}
		}).bounds(this.width / 2 - 104, this.height - 30, 100, 20).build());
		this.confirm.active = false;
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose())
			.bounds(this.width / 2 + 4, this.height - 30, 100, 20).build());
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		return this.list.isMouseOver(x, y) && this.list.mouseScrolled(x, y, scrollX, scrollY);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		BiomeEntry selected = this.list.getSelected();
		this.confirm.active = selected != null;
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		graphics.centeredText(this.font, this.title, this.width / 2, 10, 0xFFFFFFFF);
		boolean failed = selected != null && selected.index == this.menu.missingBiome();
		graphics.centeredText(this.font, failed ? MISSING_BIOME : INSTRUCTION, this.width / 2, 24, failed ? 0xFFFFAAAA : 0xFFE0E0E0);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
	}

	private final class BiomeList extends ObjectSelectionList<BiomeEntry> {
		BiomeList() {
			super(BiomeCompassScreen.this.minecraft, BiomeCompassScreen.this.width, BiomeCompassScreen.this.height - 74, 38, 18);
			for (int index = 0; index < menu.biomes().size(); index++) {
				this.addEntry(new BiomeEntry(index, BiomeCompassItem.biomeName(menu.biomes().get(index))));
			}
		}

		@Override
		public int getRowWidth() {
			return Math.min(250, this.getWidth() - 32);
		}
	}

	private final class BiomeEntry extends ObjectSelectionList.Entry<BiomeEntry> {
		private final int index;
		private final Component name;

		BiomeEntry(int index, Component name) {
			this.index = index;
			this.name = name;
		}

		@Override
		public Component getNarration() {
			return this.name;
		}

		@Override
		public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float delta) {
			graphics.text(font, this.name, this.getX() + 6, this.getY() + 5, 0xFFFFFFFF, true);
		}
	}
}
