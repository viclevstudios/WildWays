package com.viclev.wildways;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

/** Small, server-side XP rewards for exploration and everyday survival actions. */
public final class XpProgression {
	private static final TagKey<Block> ORES = TagKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("ores"));
	private static final Set<String> RARE_OR_BOSS_MOBS = Set.of(
		"minecraft:ender_dragon", "minecraft:wither", "minecraft:warden", "minecraft:elder_guardian",
		"minecraft:evoker", "minecraft:ravager", "minecraft:piglin_brute"
	);
	private static final Set<Block> WORKSTATIONS = Set.of(
		Blocks.STONECUTTER, Blocks.LOOM, Blocks.CARTOGRAPHY_TABLE, Blocks.SMITHING_TABLE,
		Blocks.GRINDSTONE, Blocks.FLETCHING_TABLE, Blocks.COMPOSTER, Blocks.LECTERN,
		Blocks.BREWING_STAND, Blocks.LODESTONE
	);

	private XpProgression() {
	}

	public static void initialize() {
		ServerLivingEntityEvents.AFTER_DEATH.register((victim, source) -> {
			Entity attacker = source.getEntity();
			if (attacker instanceof Projectile projectile && projectile.getOwner() != null) {
				attacker = projectile.getOwner();
			}
			if (attacker instanceof ServerPlayer serverPlayer) {
				awardFirstMobKill(serverPlayer, victim);
			}
		});

		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			if (player instanceof ServerPlayer serverPlayer && state.is(ORES) && !state.is(Blocks.NETHER_QUARTZ_ORE)) {
				serverPlayer.giveExperiencePoints(2);
			}
		});

		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (!(player instanceof ServerPlayer serverPlayer) || level.isClientSide()) {
				return InteractionResult.PASS;
			}
			BlockState state = level.getBlockState(hit.getBlockPos());
			Block block = state.getBlock();
			if (WORKSTATIONS.contains(block)) {
				awardOnce(serverPlayer, "workstation:" + BuiltInRegistries.BLOCK.getKey(block), 5);
			}
			if (block instanceof BeehiveBlock && state.getValue(BeehiveBlock.HONEY_LEVEL) >= 5
				&& (serverPlayer.getItemInHand(hand).is(Items.GLASS_BOTTLE) || serverPlayer.getItemInHand(hand).is(Items.SHEARS))) {
				String harvest = serverPlayer.getItemInHand(hand).is(Items.SHEARS) ? "honeycomb" : "honey";
				serverPlayer.giveExperiencePoints(2);
				awardOnce(serverPlayer, "first_honey:" + harvest, 5);
			}
			return InteractionResult.PASS;
		});

		ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) -> {
			String dimension = destination.dimension().identifier().toString();
			if (dimension.equals("minecraft:the_nether") || dimension.equals("minecraft:the_end")) {
				awardOnce(player, "portal:" + dimension, 25);
			}
		});
	}

	public static void awardFirstMobKill(ServerPlayer player, Entity victim) {
		String mob = EntityType.getKey(victim.getType()).toString();
		int xp = RARE_OR_BOSS_MOBS.contains(mob) ? 25 : victim.getType().getCategory() == MobCategory.MONSTER ? 10 : 5;
		awardOnce(player, "first_kill:" + mob, xp);
	}

	public static boolean claimFirstBreed(Animal animal, Animal other) {
		ServerPlayer player = animal.getLoveCause();
		if (player == null) {
			player = other.getLoveCause();
		}
		if (player == null) {
			return false;
		}
		String kind = EntityType.getKey(animal.getType()).toString();
		return claim(player, "first_breed:" + kind);
	}

	public static void awardFirstTame(TamableAnimal animal, Player player) {
		if (player instanceof ServerPlayer serverPlayer) {
			String kind = EntityType.getKey(animal.getType()).toString();
			awardOnce(serverPlayer, "first_tame:" + kind, 5);
		}
	}

	public static void awardFirstVillagerTrade(ServerPlayer player) {
		awardOnce(player, "first_trade:villager", 15);
	}

	public static void awardFirstPiglinTrade(Piglin piglin) {
		if (!(piglin.level() instanceof ServerLevel level)) {
			return;
		}
		Player nearest = level.getNearestPlayer(piglin.getX(), piglin.getY(), piglin.getZ(), 8.0, candidate -> candidate instanceof ServerPlayer);
		if (nearest instanceof ServerPlayer player) {
			awardOnce(player, "first_trade:piglin", 15);
		}
	}

	public static void awardEggHatch(ServerLevel level, net.minecraft.core.BlockPos pos, EntityType<?> eggType) {
		Player nearest = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 32.0,
			candidate -> candidate instanceof ServerPlayer);
		if (nearest instanceof ServerPlayer player) {
			player.giveExperiencePoints(2);
			awardOnce(player, "first_hatch:" + EntityType.getKey(eggType), 5);
		}
	}

	public static void awardNetherPortalOpening(net.minecraft.world.item.context.UseOnContext context) {
		if (!(context.getPlayer() instanceof ServerPlayer player) || !(context.getLevel() instanceof ServerLevel level)) {
			return;
		}
		net.minecraft.core.BlockPos clicked = context.getClickedPos();
		for (net.minecraft.core.BlockPos pos : net.minecraft.core.BlockPos.betweenClosed(
			clicked.offset(-2, -1, -2), clicked.offset(2, 3, 2))) {
			if (level.getBlockState(pos).is(Blocks.NETHER_PORTAL)) {
				awardOnce(player, "portal:minecraft:the_nether", 25);
				return;
			}
		}
	}

	public static void awardArmorTrim(Player player, ItemStack result) {
		if (player instanceof ServerPlayer serverPlayer && result.has(net.minecraft.core.component.DataComponents.TRIM)) {
			serverPlayer.giveExperiencePoints(5);
		}
	}

	public static void awardMetalSmelting(Player player, ItemStack result) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return;
		}
		if (result.is(Items.COPPER_INGOT) || result.is(Items.IRON_INGOT) || result.is(Items.GOLD_INGOT)
			|| result.is(Items.NETHERITE_SCRAP)) {
			serverPlayer.giveExperiencePoints(result.getCount() * 2);
		}
	}

	public static boolean isFishingTreasure(ItemStack stack) {
		return stack.is(Items.BOW) || stack.is(Items.ENCHANTED_BOOK) || stack.is(Items.FISHING_ROD)
			|| stack.is(Items.NAME_TAG) || stack.is(Items.NAUTILUS_SHELL) || stack.is(Items.SADDLE);
	}

	public static void awardOnce(ServerPlayer player, String key, int xp) {
		if (claim(player, key)) {
			player.giveExperiencePoints(xp);
		}
	}

	private static boolean claim(ServerPlayer player, String key) {
		ServerLevel overworld = player.level().getServer().overworld();
		return overworld.getDataStorage().computeIfAbsent(XpProgressionData.TYPE).claim(player.getUUID(), key);
	}
}
