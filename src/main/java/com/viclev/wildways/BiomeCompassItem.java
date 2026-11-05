package com.viclev.wildways;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public final class BiomeCompassItem extends Item {
	public static final DataComponentType<Identifier> TARGET_BIOME = Registry.register(
		BuiltInRegistries.DATA_COMPONENT_TYPE,
		Wildways.id("compass_target_biome"),
		DataComponentType.<Identifier>builder().persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC).build()
	);

	public BiomeCompassItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack compass = player.getItemInHand(hand);
		if (compass.has(TARGET_BIOME)) {
			return InteractionResult.PASS;
		}
		if (player instanceof ServerPlayer serverPlayer) {
			List<Identifier> biomes = serverPlayer.level().getChunkSource().getGenerator().getBiomeSource().possibleBiomes().stream()
				.map(Holder::unwrapKey)
				.flatMap(Optional::stream)
				.map(ResourceKey::identifier)
				.sorted()
				.toList();
			serverPlayer.openMenu(new ExtendedMenuProvider<List<Identifier>>() {
				@Override
				public Component getDisplayName() {
					return Component.translatable("wildways.biome_compass.title");
				}

				@Override
				public List<Identifier> getScreenOpeningData(ServerPlayer player) {
					return biomes;
				}

				@Override
				public BiomeCompassMenu createMenu(int id, Inventory inventory, Player player) {
					return new BiomeCompassMenu(id, biomes, hand, compass);
				}
			});
		}
		return InteractionResult.SUCCESS;
	}

	public static Component biomeName(Identifier biome) {
		return Component.translatableWithFallback(biome.toLanguageKey("biome"), biome.toString());
	}
}
