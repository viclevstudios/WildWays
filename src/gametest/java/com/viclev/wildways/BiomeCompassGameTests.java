package com.viclev.wildways;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

public class BiomeCompassGameTests {
	@GameTest
	public void bindOneCompassFromAnExistingStack(GameTestHelper helper) {
		var level = helper.getLevel();
		var player = (ServerPlayer)helper.makeMockServerPlayer(GameType.SURVIVAL);
		new ServerGamePacketListenerImpl(level.getServer(), new Connection(PacketFlow.SERVERBOUND), player,
			CommonListenerCookie.createInitial(player.getGameProfile(), false));
		BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
		player.snapTo(pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
		Identifier biome = level.getNoiseBiome(QuartPos.fromBlock(pos.getX()), QuartPos.fromBlock(pos.getY()), QuartPos.fromBlock(pos.getZ()))
			.unwrapKey().orElseThrow().identifier();

		InteractionHand hand = InteractionHand.OFF_HAND;
		ItemStack compasses = new ItemStack(ModItems.BIOME_COMPASS, 3);
		player.setItemInHand(hand, compasses);
		compasses.use(level, player, hand);
		helper.assertTrue(player.containerMenu instanceof BiomeCompassMenu, "Using the compass must open the biome menu");
		BiomeCompassMenu menu = (BiomeCompassMenu)player.containerMenu;
		int index = menu.biomes().indexOf(biome);
		helper.assertTrue(index >= 0, "The current biome must be available for selection");

		player.setItemInHand(hand, compasses.copy());
		helper.assertFalse(menu.clickMenuButton(player, index), "The open menu must reject a swapped compass stack");
		var locateBiome = Wildways.id("quests/locate_biome");
		helper.assertFalse(QuestBookItem.readProgress(player).get(locateBiome).done(), "Opening the menu or swapping the compass must not count");
		player.setItemInHand(hand, compasses);
		menu.clickMenuButton(player, index);
		helper.assertTrue(QuestBookItem.readProgress(player).get(locateBiome).done(), "Successfully binding a biome must count");
		ItemStack bound = player.getItemInHand(hand);
		helper.assertTrue(biome.equals(bound.get(BiomeCompassItem.TARGET_BIOME)) && bound.getCount() == 1,
			"Only one compass must be bound to the chosen biome");
		var tracker = bound.get(DataComponents.LODESTONE_TRACKER);
		helper.assertTrue(tracker != null && tracker.target().orElseThrow().dimension() == level.dimension() && !tracker.tracked(),
			"The needle must target the selected dimension without requiring a lodestone block");
		helper.assertTrue(player.getInventory().getNonEquipmentItems().stream()
			.anyMatch(stack -> stack.is(ModItems.BIOME_COMPASS) && stack.getCount() == 2 && !stack.has(BiomeCompassItem.TARGET_BIOME)),
			"The two remaining compasses must stay unbound");

		var ops = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
		ItemStack restored = ItemStack.CODEC.parse(ops, ItemStack.CODEC.encodeStart(ops, bound).getOrThrow()).getOrThrow();
		helper.assertTrue(ItemStack.matches(bound, restored), "Saving and loading must preserve the bound compass");
		player.setItemInHand(hand, restored);
		restored.use(level, player, hand);
		helper.assertTrue(player.containerMenu == player.inventoryMenu, "A bound compass must keep its target after another right-click");
		helper.succeed();
	}
}
