package com.viclev.wildways.client;

import com.viclev.wildways.ModBlocks;
import com.viclev.wildways.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class HeldItemInfoHud {
	private static boolean showingInformation;

	private HeldItemInfoHud() {
	}

	public static void updateActionBar(Minecraft minecraft) {
		LocalPlayer player = minecraft.player;
		if (player == null) {
			showingInformation = false;
			return;
		}

		Component information = getInformation(player);
		if (information != null) {
			minecraft.gui.hud.setOverlayMessage(information, false);
		} else if (showingInformation) {
			minecraft.gui.hud.setOverlayMessage(Component.empty(), false);
		}
		showingInformation = information != null;
	}

	private static Component getInformation(LocalPlayer player) {
		Item item = getInformationItem(player);
		BlockPos pos = player.blockPosition();
		if (item == Items.COMPASS) {
			return Component.literal("X:" + pos.getX() + " Y:" + pos.getY() + " Z:" + pos.getZ() + " | " + getDirection(player.getYRot()));
		}
		if (item == Items.CLOCK) {
			return Component.literal("Day: " + player.level().getOverworldClockTime() / 24000L);
		}
		if (item == ModItems.BIOME_COMPASS) {
			String biome = player.level().getBiome(pos).unwrapKey()
				.map(key -> formatBiomeName(key.identifier().getPath()))
				.orElse("unknown");
			return Component.literal("Biome: " + biome);
		}
		if (item == ModBlocks.LIGHT_SENSOR.asItem()) {
			return Component.literal("Light: " + player.level().getMaxLocalRawBrightness(pos));
		}
		return null;
	}

	private static String getDirection(float yaw) {
		String[] directions = { "South", "South-West", "West", "North-West", "North", "North-East", "East", "South-East" };
		int index = Math.floorMod((int) Math.floor((yaw + 22.5F) / 45.0F), directions.length);
		return directions[index];
	}

	public static boolean isInformationMessage(Component message) {
		if (message == null) {
			return false;
		}

		String text = message.getString();
		return text.startsWith("X: ")
			|| text.startsWith("Day: ")
			|| text.startsWith("Biome: ")
			|| text.startsWith("Light: ");
	}

	private static String formatBiomeName(String biomeId) {
		StringBuilder displayName = new StringBuilder();
		for (String word : biomeId.split("_")) {
			if (!displayName.isEmpty()) {
				displayName.append(' ');
			}
			displayName.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
		}
		return displayName.toString();
	}

	private static Item getInformationItem(LocalPlayer player) {
		ItemStack mainHand = player.getMainHandItem();
		if (isInformationItem(mainHand.getItem())) {
			return mainHand.getItem();
		}

		ItemStack offHand = player.getOffhandItem();
		return isInformationItem(offHand.getItem()) ? offHand.getItem() : Items.AIR;
	}

	private static boolean isInformationItem(Item item) {
		return item == Items.COMPASS
			|| item == Items.CLOCK
			|| item == ModItems.BIOME_COMPASS
			|| item == ModBlocks.LIGHT_SENSOR.asItem();
	}
}
