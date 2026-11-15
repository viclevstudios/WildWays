package com.viclev.wildways;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class WitherCombatGameTests {
	@GameTest
	public void spawnExplosionBreaksObsidianAndRespectsProtectedBlocks(GameTestHelper helper) {
		var wither = helper.spawn(EntityTypes.WITHER, new Vec3(3.5, 1.0, 3.5));
		int blastY = wither.getBlockY() - helper.absolutePos(BlockPos.ZERO).getY() + 2;
		helper.setBlock(1, blastY, 3, Blocks.OBSIDIAN);
		helper.setBlock(3, blastY, 1, Blocks.CRYING_OBSIDIAN);
		helper.setBlock(5, blastY, 3, Blocks.BEDROCK);
		helper.setBlock(3, blastY, 5, Blocks.REINFORCED_DEEPSLATE);
		wither.setInvulnerableTicks(1);
		helper.runAfterDelay(4, () -> {
			helper.assertBlockPresent(Blocks.AIR, 1, blastY, 3);
			helper.assertBlockPresent(Blocks.AIR, 3, blastY, 1);
			helper.assertBlockPresent(Blocks.BEDROCK, 5, blastY, 3);
			helper.assertBlockPresent(Blocks.REINFORCED_DEEPSLATE, 3, blastY, 5);
			wither.discard();
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 155, skyAccess = true)
	public void firstPhaseDashLandsAndAllowsMelee(GameTestHelper helper) {
		for (int x = 1; x <= 6; x++) {
			for (int z = 1; z <= 6; z++) {
				helper.setBlock(x, 1, z, Blocks.BEDROCK);
			}
		}
		var level = helper.getLevel();
		var player = helper.makeMockPlayer(GameType.SURVIVAL);
		Vec3 feet = helper.absoluteVec(new Vec3(3.5, 2.0, 3.5));
		player.setPos(feet);
		Vec3 takeoff = feet.add(0, 5, 0);
		var wither = new WitherBoss(EntityTypes.WITHER, level) {
			@Override
			public LivingEntity getTarget() {
				return player;
			}
		};
		wither.setPos(takeoff);
		level.addFreshEntity(wither);
		helper.onEachTick(() -> {
			// Hold the starting altitude until the opening cooldown expires.
			if (wither.tickCount < 80) {
				wither.setPos(takeoff);
				wither.setDeltaMovement(Vec3.ZERO);
			}
		});
		helper.runAfterDelay(96, () -> helper.assertTrue(Math.abs(wither.getY() - takeoff.y) < 0.25,
			"The Wither should hover during the warning before it dives"));
		helper.startSequence().thenWaitUntil(() -> helper.assertTrue(
				wither.tickCount >= 100 && wither.getY() < feet.y + 0.2, "The Wither has not landed yet"))
			.thenExecuteAfter(12, () -> {
				helper.assertTrue(wither.getY() < feet.y + 0.2, "The Wither should stay in reach after landing");
				helper.assertTrue(wither.hurtServer(level, level.damageSources().playerAttack(player), 4.0F),
					"The Wither should take melee damage during recovery");
				wither.discard();
			}).thenSucceed();
	}
}
