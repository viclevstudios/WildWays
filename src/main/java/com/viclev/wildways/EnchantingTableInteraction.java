package com.viclev.wildways;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.Blocks;

public final class EnchantingTableInteraction {
	private EnchantingTableInteraction() {
	}

	public static void initialize() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (hand != InteractionHand.MAIN_HAND || player.isSpectator()
				|| !level.getBlockState(hit.getBlockPos()).is(Blocks.ENCHANTING_TABLE)) {
				return InteractionResult.PASS;
			}
			// Preserve sneaking to place blocks, while empty-handed sneaking also uses this menu.
			if (player.isSecondaryUseActive() && (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty())) {
				return InteractionResult.PASS;
			}
			if (level instanceof ServerLevel serverLevel) {
				player.openMenu(new SimpleMenuProvider(
					(id, inventory, owner) -> new WildwaysEnchantingMenu(id, inventory, ContainerLevelAccess.create(serverLevel, hit.getBlockPos())),
					Component.translatable("block.minecraft.enchanting_table")
				));
			}
			return InteractionResult.SUCCESS;
		});
	}
}
