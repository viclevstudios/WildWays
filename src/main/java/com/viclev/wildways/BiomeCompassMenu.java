package com.viclev.wildways;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Prediction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public final class BiomeCompassMenu extends AbstractContainerMenu {
	private final List<Identifier> biomes;
	private final InteractionHand hand;
	private final ItemStack compass;
	private final DataSlot missingBiome = DataSlot.standalone();
	private final DataSlot searching = DataSlot.standalone();
	private @Nullable BiomeCompassSearch search;
	private @Nullable ServerLevel searchLevel;
	private int selectedBiome;

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
		this.addDataSlot(this.searching);
	}

	public List<Identifier> biomes() {
		return this.biomes;
	}

	public int missingBiome() {
		return this.missingBiome.get();
	}

	public boolean searching() {
		return this.searching.get() != 0;
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
		if (!(player instanceof ServerPlayer serverPlayer) || player.containerMenu != this || this.search != null
			|| !this.stillValid(player) || index < 0 || index >= this.biomes.size()
			|| player.getCooldowns().isOnCooldown(this.compass)) {
			return false;
		}

		var level = serverPlayer.level();
		var biome = ResourceKey.create(Registries.BIOME, this.biomes.get(index));
		this.missingBiome.set(-1);
		this.selectedBiome = index;
		this.searchLevel = level;
		this.search = new BiomeCompassSearch(level, player.blockPosition(), biome);
		this.searching.set(1);
		BiomeCompassSearch.enqueue(serverPlayer, this);
		return true;
	}

	boolean advanceSearch(ServerPlayer player, long deadline) {
		var search = this.search;
		if (search == null || player.isRemoved() || player.containerMenu != this || player.level() != this.searchLevel || !this.stillValid(player)) {
			this.clearSearch();
			return false;
		}
		if (!search.advance(deadline)) {
			return true;
		}
		BlockPos target = search.result();
		this.clearSearch();
		if (target == null) {
			this.missingBiome.set(this.selectedBiome);
			player.getCooldowns().addCooldown(this.compass, 20);
		} else {
			this.bindCompass(player, this.biomes.get(this.selectedBiome), target);
		}
		return false;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		if (this.searchLevel != null) {
			BiomeCompassSearch.cancel(this.searchLevel, this);
		}
		this.clearSearch();
	}

	private void clearSearch() {
		this.search = null;
		this.searchLevel = null;
		this.searching.set(0);
	}

	private void bindCompass(ServerPlayer player, Identifier biome, BlockPos target) {
		ItemStack boundCompass = this.compass.copyWithCount(1);
		boundCompass.set(BiomeCompassItem.TARGET_BIOME, biome);
		boundCompass.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(GlobalPos.of(player.level().dimension(), target)), false));
		boundCompass.set(DataComponents.MAX_STACK_SIZE, 1);
		this.compass.shrink(1);
		player.setItemInHand(this.hand, boundCompass);
		if (!this.compass.isEmpty() && !player.getInventory().add(this.compass)) {
			player.drop(this.compass, false, Prediction.SERVER_ONLY);
		}
		QuestBookItem.awardQuest(player, Wildways.id("quests/locate_biome"));
		player.closeContainer();
	}
}
