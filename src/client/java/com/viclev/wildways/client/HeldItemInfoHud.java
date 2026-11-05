package com.viclev.wildways.client;

import com.viclev.wildways.BiomeCompassItem;
import com.viclev.wildways.ModBlocks;
import com.viclev.wildways.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
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
		ItemStack stack = getInformationStack(player);
		Item item = stack.getItem();
		BlockPos pos = player.blockPosition();
		if (item == Items.COMPASS) {
			return Component.literal("X:" + pos.getX() + " Y:" + pos.getY() + " Z:" + pos.getZ() + " | " + getDirection(player.getYRot()));
		}
		if (item == Items.CLOCK) {
			return Component.literal("Day: " + player.level().getOverworldClockTime() / 24000L);
		}
		if (item == ModItems.BIOME_COMPASS) {
			Component biome = player.level().getBiome(pos).unwrapKey()
				.map(key -> BiomeCompassItem.biomeName(key.identifier()))
				.orElseGet(() -> Component.translatable("wildways.biome_compass.unknown"));
			Identifier target = stack.get(BiomeCompassItem.TARGET_BIOME);
			return target == null ? Component.translatable("wildways.biome_compass.current", biome)
				: Component.translatable("wildways.biome_compass.current_and_target", biome, BiomeCompassItem.biomeName(target));
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
		if (message.getContents() instanceof TranslatableContents translated) {
			return translated.getKey().equals("wildways.biome_compass.current")
				|| translated.getKey().equals("wildways.biome_compass.current_and_target");
		}

		String text = message.getString();
		return text.startsWith("X: ")
			|| text.startsWith("Day: ")
			|| text.startsWith("Light: ");
	}

	private static ItemStack getInformationStack(LocalPlayer player) {
		ItemStack mainHand = player.getMainHandItem();
		if (isInformationItem(mainHand.getItem())) {
			return mainHand;
		}

		ItemStack offHand = player.getOffhandItem();
		return isInformationItem(offHand.getItem()) ? offHand : ItemStack.EMPTY;
	}

	private static boolean isInformationItem(Item item) {
		return item == Items.COMPASS
			|| item == Items.CLOCK
			|| item == ModItems.BIOME_COMPASS
			|| item == ModBlocks.LIGHT_SENSOR.asItem();
	}
}
