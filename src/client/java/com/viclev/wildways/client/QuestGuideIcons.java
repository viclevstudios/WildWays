package com.viclev.wildways.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.viclev.wildways.Wildways;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

final class QuestGuideIcons {
	private static final Identifier RESOURCE = Wildways.id("quest_guide/icons.json");
	private static Map<String, Icon> overrides = Map.of();

	private QuestGuideIcons() {
	}

	static void initialize() {
		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(Wildways.id("quest_guide_icons"),
			(ResourceManagerReloadListener)QuestGuideIcons::reload);
	}

	static Icon icon(String key, Item fallback) {
		Icon override = overrides.get(key);
		return override == null ? new Icon(fallback, null) : override;
	}

	private static void reload(ResourceManager resources) {
		Map<String, Icon> icons = new HashMap<>();
		for (var resource : resources.getResourceStack(RESOURCE)) {
			try (var reader = resource.openAsReader()) {
				if (!(JsonParser.parseReader(reader) instanceof JsonObject entries)) {
					Wildways.LOGGER.warn("Expected an object in {} from {}", RESOURCE, resource.sourcePackId());
					continue;
				}
				for (var entry : entries.entrySet()) {
					Icon icon = entry.getValue() instanceof JsonObject definition ? parseIcon(definition, resources) : null;
					if (icon == null) {
						Wildways.LOGGER.warn("Invalid quest guide icon {} in {}", entry.getKey(), resource.sourcePackId());
					} else {
						icons.put(entry.getKey(), icon);
					}
				}
			} catch (IOException | JsonParseException exception) {
				Wildways.LOGGER.warn("Could not load {} from {}", RESOURCE, resource.sourcePackId(), exception);
			}
		}
		overrides = Map.copyOf(icons);
	}

	private static @Nullable Icon parseIcon(JsonObject definition, ResourceManager resources) {
		if (definition.has("item") == definition.has("texture")) {
			return null;
		}
		boolean texture = definition.has("texture");
		var value = definition.get(texture ? "texture" : "item");
		Identifier id = value.isJsonPrimitive() && value.getAsJsonPrimitive().isString() ? Identifier.tryParse(value.getAsString()) : null;
		if (id == null) {
			return null;
		}
		if (texture) {
			return resources.getResource(id).isPresent() ? new Icon(null, id) : null;
		}
		return BuiltInRegistries.ITEM.getOptional(id).map(item -> new Icon(item, null)).orElse(null);
	}

	record Icon(@Nullable Item item, @Nullable Identifier texture) {
		void draw(GuiGraphicsExtractor graphics, int x, int y) {
			if (this.texture != null) {
				graphics.blit(this.texture, x, y, x + 16, y + 16, 0, 1, 0, 1);
			} else if (this.item != null) {
				graphics.item(this.item.getDefaultInstance(), x, y);
			}
		}
	}
}
