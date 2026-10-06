package com.viclev.wildways;

import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;

public class EndermiteBoxGameTests {
	@GameTest
	public void creativeBreakDropsEmptyAndFilledNest(GameTestHelper helper) {
		Player player = helper.makeMockPlayer(GameType.CREATIVE);
		player.getAbilities().instabuild = true;
		ItemStack emptyNest = this.breakNest(helper, player, new BlockPos(1, 2, 1), false);
		helper.assertTrue(emptyNest.get(DataComponents.CONTAINER) == null
			|| emptyNest.get(DataComponents.CONTAINER).nonEmptyItemCopyStream().findAny().isEmpty(),
			"An empty nest should drop without contents");

		ItemStack filledNest = this.breakNest(helper, player, new BlockPos(3, 2, 1), true);
		helper.assertTrue(filledNest.get(DataComponents.CONTAINER) != null
			&& filledNest.get(DataComponents.CONTAINER).nonEmptyItemCopyStream()
				.anyMatch(stack -> stack.is(Items.DIAMOND) && stack.getCount() == 3),
			"A creative-broken nest should retain its contents");
		helper.succeed();
	}

	private ItemStack breakNest(GameTestHelper helper, Player player, BlockPos relativePos, boolean filled) {
		helper.setBlock(relativePos, ModBlocks.ENDERMITE_BOX);
		BlockPos pos = helper.absolutePos(relativePos);
		EndermiteBoxBlockEntity nest = (EndermiteBoxBlockEntity) helper.getLevel().getBlockEntity(pos);
		if (filled) {
			nest.setItem(0, new ItemStack(Items.DIAMOND, 3));
		}
		ModBlocks.ENDERMITE_BOX.playerWillDestroy(helper.getLevel(), pos, helper.getLevel().getBlockState(pos), player);
		List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos),
			entity -> entity.getItem().is(ModBlocks.ENDERMITE_BOX.asItem()));
		helper.assertTrue(drops.size() == 1, "Breaking an endermite nest in creative should drop exactly one nest");
		return drops.getFirst().getItem();
	}
}
