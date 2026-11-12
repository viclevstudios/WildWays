package com.viclev.wildways;

import java.util.Set;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class EquipmentQuests {
	private static final Set<Item> COPPER_TOOLS = Set.of(Items.COPPER_PICKAXE, Items.COPPER_AXE, Items.COPPER_SHOVEL,
		Items.COPPER_HOE, Items.COPPER_SWORD, Items.COPPER_SPEAR);
	private static final Identifier MINE_WITH_COPPER = Wildways.id("quests/mine_with_copper");
	private static final Identifier AMETHYST_COMBAT = Wildways.id("quests/amethyst_combat");
	private static final Identifier FLETCH_ARROWS = Wildways.id("quests/fletch_arrows");
	private static final Identifier FLETCH_TURTLE_ARROW = Wildways.id("quests/fletch_turtle_arrow");
	private static final Identifier FLETCH_RANGE_ARROW = Wildways.id("quests/fletch_range_arrow");
	private static final Identifier FLETCH_EXPLOSIVE_ARROW = Wildways.id("quests/fletch_explosive_arrow");

	private EquipmentQuests() {
	}

	static void initialize() {
		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			if (player instanceof ServerPlayer serverPlayer && COPPER_TOOLS.contains(player.getMainHandItem().getItem())) {
				QuestBookItem.awardQuest(serverPlayer, MINE_WITH_COPPER);
			}
		});
		ServerLivingEntityEvents.AFTER_DAMAGE.register((target, source, baseDamage, damage, blocked) -> {
			if (damage > 0 && !blocked) {
				creditAmethystAttack(source);
			}
		});
		ServerLivingEntityEvents.AFTER_DEATH.register((target, source) -> creditAmethystAttack(source));
	}

	private static void creditAmethystAttack(DamageSource source) {
		if (!(source.getDirectEntity() instanceof ServerPlayer player)) {
			return;
		}
		boolean spearAttack = source.is(DamageTypes.SPEAR);
		if (!spearAttack && !source.is(DamageTypes.PLAYER_ATTACK)) {
			return;
		}
		ItemStack active = player.getUseItem();
		ItemStack weapon = spearAttack && active.has(DataComponents.KINETIC_WEAPON) ? active : player.getMainHandItem();
		if (ProgressionItems.isAmethystTool(weapon.getItem())) {
			QuestBookItem.awardQuest(player, AMETHYST_COMBAT);
		}
	}

	static void fletched(ServerPlayer player, ItemStack result) {
		QuestBookItem.awardQuest(player, FLETCH_ARROWS);
		if (result.is(ModItems.TURTLE_ARROW)) {
			QuestBookItem.awardQuest(player, FLETCH_TURTLE_ARROW);
		} else if (result.is(ModItems.RANGE_ARROW)) {
			QuestBookItem.awardQuest(player, FLETCH_RANGE_ARROW);
		} else if (result.is(ModItems.EXPLOSIVE_ARROW)) {
			QuestBookItem.awardQuest(player, FLETCH_EXPLOSIVE_ARROW);
		}
	}
}
