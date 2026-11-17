package com.viclev.wildways.client;

import com.viclev.wildways.BiomeCompassItem;
import com.viclev.wildways.ModBlocks;
import com.viclev.wildways.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public final class HeldItemInfoHud {
	private static final String[] DIRECTIONS = { "South", "South-West", "West", "North-West", "North", "North-East", "East", "South-East" };
	private static @Nullable Component information;

	private HeldItemInfoHud() {
	}

	public static void update(Minecraft minecraft) {
		LocalPlayer player = minecraft.player;
		information = player == null ? null : getInformation(player);
	}

	public static @Nullable Component information() {
		return information;
	}

	private static @Nullable Component getInformation(LocalPlayer player) {
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
		int index = Math.floorMod((int) Math.floor((yaw + 22.5F) / 45.0F), DIRECTIONS.length);
		return DIRECTIONS[index];
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
