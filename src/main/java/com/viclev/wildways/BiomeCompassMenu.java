package com.viclev.wildways;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Prediction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;

import java.util.List;
import java.util.Optional;

public final class BiomeCompassMenu extends AbstractContainerMenu {
	private final List<Identifier> biomes;
	private final InteractionHand hand;
	private final ItemStack compass;
	private final DataSlot missingBiome = DataSlot.standalone();

	public BiomeCompassMenu(int id, Inventory inventory, List<Identifier> biomes) {
		this(id, biomes, InteractionHand.MAIN_HAND, ItemStack.EMPTY);
	}

	BiomeCompassMenu(int id, List<Identifier> biomes, InteractionHand hand, ItemStack compass) {
		super(ModMenuTypes.BIOME_COMPASS, id);
		this.biomes = biomes;
		this.hand = hand;
		this.compass = compass;
		this.missingBiome.set(-1);
		this.addDataSlot(this.missingBiome);
	}

	public List<Identifier> biomes() {
		return this.biomes;
	}

	public int missingBiome() {
		return this.missingBiome.get();
	}

	@Override
	public boolean stillValid(Player player) {
		return player.level().isClientSide() || (player.getItemInHand(this.hand) == this.compass
			&& this.compass.is(ModItems.BIOME_COMPASS) && !this.compass.has(BiomeCompassItem.TARGET_BIOME));
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean clickMenuButton(Player player, int index) {
		if (!(player instanceof ServerPlayer serverPlayer) || !this.stillValid(player) || index < 0 || index >= this.biomes.size()
			|| player.getCooldowns().isOnCooldown(this.compass)) {
			return false;
		}

		var level = serverPlayer.level();
		Identifier biome = this.biomes.get(index);
		var biomeKey = ResourceKey.create(Registries.BIOME, biome);
		// Use the same search radius and sampling steps as /locate biome.
		var target = level.findClosestBiome3d(holder -> holder.is(biomeKey), player.blockPosition(), 6400, 32, 64);
		if (target == null) {
			this.missingBiome.set(index);
			player.getCooldowns().addCooldown(this.compass, 20);
			return true;
		}

		ItemStack boundCompass = this.compass.copyWithCount(1);
		boundCompass.set(BiomeCompassItem.TARGET_BIOME, biome);
		boundCompass.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(GlobalPos.of(level.dimension(), target.getFirst())), false));
		boundCompass.set(DataComponents.MAX_STACK_SIZE, 1);
		this.compass.shrink(1);
		player.setItemInHand(this.hand, boundCompass);
		if (!this.compass.isEmpty() && !player.getInventory().add(this.compass)) {
			player.drop(this.compass, false, Prediction.SERVER_ONLY);
		}
		serverPlayer.closeContainer();
		return false;
	}
}
