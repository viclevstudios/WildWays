package com.viclev.wildways;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.Blocks;

/** Opens a server-owned transient menu; the table remains a Fletcher workstation. */
public final class FletchingTableInteraction {
	private static final Component TITLE = Component.translatable("block.minecraft.fletching_table");

	private FletchingTableInteraction() {
	}

	public static void initialize() {
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			if (hand != InteractionHand.MAIN_HAND || !level.getBlockState(hitResult.getBlockPos()).is(Blocks.FLETCHING_TABLE)) {
				return InteractionResult.PASS;
			}

			if (level instanceof ServerLevel serverLevel) {
				BlockPos pos = hitResult.getBlockPos();
				MenuProvider provider = new SimpleMenuProvider(
					(containerId, inventory, menuPlayer) -> new FletchingTableMenu(containerId, inventory, ContainerLevelAccess.create(serverLevel, pos)),
					TITLE
				);
				player.openMenu(provider);
			}

			return InteractionResult.SUCCESS;
		});
	}
}
