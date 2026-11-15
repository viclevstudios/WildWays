package com.viclev.wildways;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.EntityHitResult;

public class AdvancementGameTests {
	@GameTest(maxTicks = 60)
	public void solarPoweredRequiresAnActualComparatorOutput(GameTestHelper helper) {
		var player = helper.makeMockServerPlayerInLevel();
		var advancement = helper.getLevel().getServer().getAdvancements().get(Wildways.id("adventure/solar_powered"));
		var progress = player.getAdvancements().getOrStartProgress(advancement);
		var sensor = new BlockPos(1, 2, 1);
		var comparator = sensor.east();
		var playerPos = helper.absolutePos(sensor.north());
		player.snapTo(playerPos.getX(), playerPos.getY(), playerPos.getZ(), 0, 0);
		helper.setBlock(comparator.below(), Blocks.STONE);
		helper.setBlock(sensor, ModBlocks.LIGHT_SENSOR);
		helper.setBlock(sensor.above(), Blocks.GLOWSTONE);
		var comparatorState = Blocks.COMPARATOR.defaultBlockState();
		helper.setBlock(comparator, comparatorState.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
		helper.runAfterDelay(25, () -> {
			helper.assertFalse(progress.isDone(), "A lit sensor beside a comparator facing the wrong way must not count");
			helper.setBlock(comparator, Blocks.AIR);
			var aligned = comparatorState.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST);
			helper.setBlock(comparator, aligned);
			var level = helper.getLevel();
			var comparatorPos = helper.absolutePos(comparator);
			Blocks.COMPARATOR.setPlacedBy(level, comparatorPos, aligned, player, new ItemStack(Items.COMPARATOR));
			helper.succeedWhen(() -> {
				helper.assertTrue(level.getSignal(comparatorPos, Direction.WEST) > 0, "The aligned comparator must output a signal");
				helper.assertTrue(progress.isDone(), "A comparator reading the lit sensor must count");
			});
		});
	}

	@GameTest
	public void revengeRequiresADirectExplosiveArrowHitOnACreeper(GameTestHelper helper) {
		var player = helper.makeMockServerPlayerInLevel();
		var level = helper.getLevel();
		var advancement = level.getServer().getAdvancements().get(Wildways.id("adventure/revenge"));
		var progress = player.getAdvancements().getOrStartProgress(advancement);
		var creeper = helper.spawn(EntityTypes.CREEPER, new BlockPos(1, 2, 1));
		var arrow = new ExplosiveArrowEntity(level, player, new ItemStack(ModItems.EXPLOSIVE_ARROW), new ItemStack(Items.BOW));
		SpecialArrowAdvancements.triggerTakeAim(player, creeper, level.damageSources().explosion(arrow, player));
		helper.assertFalse(progress.isDone(), "Splash damage from the arrow's explosion must not count");
		arrow.setPos(creeper.position());
		arrow.onHit(new EntityHitResult(creeper));
		helper.assertTrue(progress.isDone(), "A direct explosive arrow hit on a creeper must count");
		helper.succeed();
	}
}
