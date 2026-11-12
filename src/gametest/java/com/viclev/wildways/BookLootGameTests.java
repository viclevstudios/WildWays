package com.viclev.wildways;

import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Exercises the loaded loot tables, including existing Bastion rewards. */
public class BookLootGameTests {
	@GameTest
	public void collectorEyesCanBeFoundInTempleAndBunker(GameTestHelper helper) {
		LootParams params = new LootParams.Builder(helper.getLevel())
			.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(BlockPos.ZERO)))
			.create(LootContextParamSets.CHEST);
		var temple = table(helper, "minecraft:chests/jungle_temple");
		var bunker = table(helper, "wildways:chests/quarantine_grounds/secret");
		RandomSource random = RandomSource.create(42);
		int chestsWithEye = 0;
		for (int sample = 0; sample < 256; sample++) {
			int eyes = temple.getRandomItems(params, random).stream()
				.filter(stack -> stack.is(ModItems.EYE_OF_THE_TIGER)).mapToInt(ItemStack::getCount).sum();
			helper.assertTrue(eyes <= 1, "Each chest can contain at most one Eye of the Tiger");
			chestsWithEye += eyes;
		}
		helper.assertTrue(chestsWithEye >= 102 && chestsWithEye <= 154,
			"The Eye of the Tiger should appear in about half of Jungle Pyramid chests, got " + chestsWithEye);
		helper.assertTrue(bunker.getRandomItems(params, RandomSource.create(1)).stream()
			.anyMatch(stack -> stack.is(ModItems.LOST_EYE)), "The secret bunker should contain the Lost Eye");
		helper.succeed();
	}

	@GameTest
	public void jungleTempleTrapsLoadOneSpecialArrowType(GameTestHelper helper) {
		LootParams params = new LootParams.Builder(helper.getLevel())
			.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(BlockPos.ZERO)))
			.create(LootContextParamSets.CHEST);
		LootTable trap = table(helper, "minecraft:chests/jungle_temple_dispenser");
		Set<Item> expected = Set.of(Items.TIPPED_ARROW, ModItems.TURTLE_ARROW, ModItems.EXPLOSIVE_ARROW);
		Set<Item> seen = new HashSet<>();
		RandomSource random = RandomSource.create(42);
		boolean sawExtraArrows = false;
		for (int sample = 0; sample < 128; sample++) {
			var loot = trap.getRandomItems(params, random);
			int count = loot.stream().mapToInt(ItemStack::getCount).sum();
			helper.assertTrue(count >= 2 && count <= 14, "A trap must keep the vanilla ammunition range");
			sawExtraArrows |= count > 7;
			Item arrow = loot.getFirst().getItem();
			helper.assertTrue(expected.contains(arrow) && loot.stream().allMatch(stack -> stack.is(arrow)),
				"A trap must contain exactly one of the special arrow types");
			for (ItemStack stack : loot) {
				if (stack.is(Items.TIPPED_ARROW)) {
					helper.assertTrue(stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
						.potion().filter(Potions.POISON::equals).isPresent(), "Tipped arrows in the trap must apply Poison");
				}
			}
			seen.add(arrow);
		}
		helper.assertTrue(seen.equals(expected), "All three arrow types must be reachable");
		helper.assertTrue(sawExtraArrows, "A trap must be able to generate a second roll of arrows");
		helper.succeed();
	}

	@GameTest
	public void quarantineNestContainsOneOrTwoCommonBooks(GameTestHelper helper) {
		LootParams params = new LootParams.Builder(helper.getLevel())
			.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(BlockPos.ZERO)))
			.create(LootContextParamSets.CHEST);
		LootTable nest = table(helper, "wildways:chests/quarantine_grounds/secret");
		Set<ResourceKey<Enchantment>> allowed = Set.of(Enchantments.UNBREAKING, Enchantments.PROTECTION,
			Enchantments.EFFICIENCY, Enchantments.FORTUNE, Enchantments.SILK_TOUCH,
			Enchantments.SHARPNESS, Enchantments.POWER);
		Set<ResourceKey<Enchantment>> seen = new HashSet<>();
		boolean retainedOriginalLoot = false;
		for (int seed = 0; seed < 200; seed++) {
			var drops = nest.getRandomItems(params, RandomSource.create(seed));
			int count = drops.stream().filter(stack -> stack.is(Items.ENCHANTED_BOOK))
				.mapToInt(ItemStack::getCount).sum();
			helper.assertTrue(count >= 1 && count <= 2, "A Quarantine Grounds nest must contain one or two books");
			for (ItemStack stack : drops) {
				if (!stack.is(Items.ENCHANTED_BOOK)) {
					retainedOriginalLoot = true;
					continue;
				}
				ItemEnchantments enchantments = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
				helper.assertTrue(enchantments.size() == 1,
					"Each nest reward must be a single-enchantment book");
				var enchantment = enchantments.keySet().iterator().next();
				helper.assertTrue(enchantments.getLevel(enchantment) == 1,
					"Nest books must be level-one recipes");
				boolean permitted = false;
				for (ResourceKey<Enchantment> key : allowed) {
					if (enchantment.is(key)) {
						seen.add(key);
						permitted = true;
					}
				}
				helper.assertTrue(permitted, "Nest loot must stay inside the seven-book whitelist");
			}
		}
		helper.assertTrue(seen.equals(allowed), "All seven common books must be reachable");
		helper.assertTrue(retainedOriginalLoot, "The nest must retain its pre-existing non-book loot");
		helper.succeed();
	}

	@GameTest
	public void lightningTurnsOneDroppedBookIntoChanneling(GameTestHelper helper) {
		ItemEntity item = helper.spawn(EntityTypes.ITEM, new BlockPos(2, 2, 2));
		item.setItem(new ItemStack(Items.BOOK));
		var lightning = helper.spawn(EntityTypes.LIGHTNING_BOLT, new BlockPos(2, 2, 2));
		item.thunderHit(helper.getLevel(), lightning);
		var channeling = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
			.getOrThrow(Enchantments.CHANNELING);
		helper.assertTrue(item.getItem().getOrDefault(DataComponents.STORED_ENCHANTMENTS,
			ItemEnchantments.EMPTY).getLevel(channeling) == 1,
			"A lightning-struck ordinary book must become a Channeling book");
		helper.succeed();
	}

	@GameTest
	public void mobLootKeepsKillConditionsAndSpecialDrops(GameTestHelper helper) {
		var level = helper.getLevel();
		var player = helper.makeMockPlayer(GameType.SURVIVAL);
		var endermite = helper.spawn(EntityTypes.ENDERMITE, new BlockPos(1, 2, 1));
		var parched = helper.spawn(EntityTypes.PARCHED, new BlockPos(2, 2, 1));
		var shellTable = table(helper, "minecraft:entities/endermite");
		var parchedTable = table(helper, "minecraft:entities/parched");
		var power = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.POWER);
		var endermiteLoot = mobLoot(helper, endermite, null);
		var parchedLoot = mobLoot(helper, parched, null);
		var playerEndermiteLoot = mobLoot(helper, endermite, player);
		var playerParchedLoot = mobLoot(helper, parched, player);

		Set<Integer> shellCounts = new HashSet<>();
		boolean hasPowerBook = false;
		boolean hasWeaknessArrow = false;
		for (int roll = 1; roll <= 256; roll++) {
			long seed = roll * 104729L;
			helper.assertTrue(shellTable.getRandomItems(endermiteLoot, seed).isEmpty(),
				"An Endermite must need a player kill to drop shells");
			shellCounts.add(shellTable.getRandomItems(playerEndermiteLoot, seed).stream()
				.filter(item -> item.is(ModItems.ENDERMITE_SHELL)).mapToInt(ItemStack::getCount).sum());
			helper.assertTrue(parchedTable.getRandomItems(parchedLoot, seed).stream().noneMatch(item -> item.is(Items.ENCHANTED_BOOK)),
				"A Parched must need a player kill to drop a Power book");
			for (ItemStack drop : parchedTable.getRandomItems(playerParchedLoot, seed)) {
				helper.assertFalse(drop.is(Items.BOOK), "A Parched book drop must be enchanted");
				hasPowerBook |= drop.is(Items.ENCHANTED_BOOK)
					&& drop.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).getLevel(power) == 1;
				hasWeaknessArrow |= drop.is(Items.TIPPED_ARROW)
					&& drop.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion().filter(Potions.WEAKNESS::equals).isPresent();
			}
		}
		helper.assertTrue(shellCounts.equals(Set.of(0, 1, 2)), "Player kills should roll between zero and two shells");
		helper.assertTrue(hasPowerBook && hasWeaknessArrow, "Parched must retain both Power books and vanilla Weakness arrows");
		helper.succeed();
	}

	private static LootParams mobLoot(GameTestHelper helper, LivingEntity mob, @Nullable Player player) {
		var level = helper.getLevel();
		return new LootParams.Builder(level)
			.withParameter(LootContextParams.ORIGIN, mob.position())
			.withParameter(LootContextParams.THIS_ENTITY, mob)
			.withParameter(LootContextParams.DAMAGE_SOURCE, player == null ? level.damageSources().generic() : level.damageSources().playerAttack(player))
			.withOptionalParameter(LootContextParams.ATTACKING_ENTITY, player)
			.withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
			.create(LootContextParamSets.ENTITY);
	}

	@GameTest
	public void combatBooksUseTheMobsStateAtDeath(GameTestHelper helper) {
		var level = helper.getLevel();
		var source = level.damageSources().playerAttack(helper.makeMockPlayer(GameType.SURVIVAL));
		BlockPos position = new BlockPos(2, 2, 2);
		var raid = new Raid(helper.absolutePos(position), level.getDifficulty());
		RandomSource seeds = RandomSource.create(42L);
		for (var type : java.util.List.of(EntityTypes.PILLAGER, EntityTypes.VINDICATOR)) {
			for (int attempt = 0; attempt < 128; attempt++) {
				var raider = helper.spawn(type, position);
				raider.setCurrentRaid(raid);
				raider.setWave(1);
				raid.addWaveMob(level, 1, raider, true);
				raider.getRandom().setSeed(seeds.nextLong());
				raider.hurtServer(level, source, 1000);
				helper.assertFalse(raider.hasActiveRaid(), "A dead raider must have left the raid");
			}
		}
		for (int attempt = 0; attempt < 16; attempt++) {
			var zombie = helper.spawn(EntityTypes.ZOMBIE, position);
			zombie.setBaby(false);
			zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SPEAR));
			zombie.setDropChance(EquipmentSlot.MAINHAND, 2.0F);
			zombie.hurtServer(level, source, 1000);
			helper.assertTrue(zombie.getMainHandItem().isEmpty(), "A zombie that drops its spear must clear the hand slot");
		}
		var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		var piercing = enchantments.getOrThrow(Enchantments.PIERCING);
		var lunge = enchantments.getOrThrow(Enchantments.LUNGE);
		var books = helper.findEntities(EntityTypes.ITEM, Vec3.atCenterOf(position), 3).stream()
			.map(ItemEntity::getItem)
			.filter(stack -> stack.is(Items.ENCHANTED_BOOK))
			.map(stack -> stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)).toList();
		helper.assertTrue(books.stream().anyMatch(book -> book.getLevel(piercing) == 1),
			"Killing active raiders must be able to drop Piercing");
		for (var key : java.util.List.of(Enchantments.QUICK_CHARGE, Enchantments.MULTISHOT)) {
			var enchantment = enchantments.getOrThrow(key);
			helper.assertTrue(books.stream().anyMatch(book -> book.getLevel(enchantment) == 1),
				"Killing active raiders must be able to drop " + key.identifier());
		}
		helper.assertTrue(books.stream().anyMatch(book -> book.getLevel(lunge) == 1),
			"Zombies that drop their spear must still be able to drop Lunge");
		helper.succeed();
	}

	@GameTest
	public void illagersOutsideRaidsDropNoBooks(GameTestHelper helper) {
		var level = helper.getLevel();
		var source = level.damageSources().playerAttack(helper.makeMockPlayer(GameType.SURVIVAL));
		BlockPos position = new BlockPos(2, 2, 2);
		RandomSource seeds = RandomSource.create(42L);
		for (var type : java.util.List.of(EntityTypes.PILLAGER, EntityTypes.VINDICATOR)) {
			for (int attempt = 0; attempt < 32; attempt++) {
				var illager = helper.spawn(type, position);
				illager.getRandom().setSeed(seeds.nextLong());
				illager.hurtServer(level, source, 1000);
			}
		}
		helper.assertFalse(helper.findEntities(EntityTypes.ITEM, Vec3.atCenterOf(position), 3).stream()
			.anyMatch(item -> item.getItem().is(Items.ENCHANTED_BOOK)), "Player kills outside raids must never drop enchanted books");
		helper.succeed();
	}

	@GameTest
	public void structureBookTablesAndBastionLootAreLoaded(GameTestHelper helper) {
		LootParams params = new LootParams.Builder(helper.getLevel())
			.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(net.minecraft.core.BlockPos.ZERO)))
			.create(LootContextParamSets.CHEST);
		assertBookAppears(helper, params, "wildways:chests/dungeon_zombie", Enchantments.SMITE, 100);
		assertBookAppears(helper, params, "wildways:chests/jungle_hidden_room", Enchantments.PROJECTILE_PROTECTION, 100);
		assertBookAppears(helper, params, "wildways:chests/monument_supply", Enchantments.DEPTH_STRIDER, 100);
		LootTable bastion = table(helper, "minecraft:chests/bastion_other");
		boolean volcanite = false;
		for (int seed = 0; seed < 200; seed++) {
			volcanite |= bastion.getRandomItems(params, RandomSource.create(seed)).stream()
				.anyMatch(stack -> stack.is(ModItems.VOLCANITE));
		}
		helper.assertTrue(volcanite, "The earlier Volcanite Bastion pool must survive the book table override");
		helper.succeed();
	}

	private static void assertBookAppears(GameTestHelper helper, LootParams params, String path,
		ResourceKey<Enchantment> enchantment, int samples) {
		LootTable table = table(helper, path);
		var holder = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment);
		boolean found = false;
		for (int seed = 0; seed < samples; seed++) {
			for (ItemStack stack : table.getRandomItems(params, RandomSource.create(seed))) {
				if (stack.is(Items.ENCHANTED_BOOK) && stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS,
					ItemEnchantments.EMPTY).getLevel(holder) == 1) {
					found = true;
				}
			}
		}
		helper.assertTrue(found, path + " must produce the planned book");
	}

	private static LootTable table(GameTestHelper helper, String path) {
		ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, Identifier.parse(path));
		return helper.getLevel().getServer().reloadableRegistries().getLootTable(key);
	}
}
