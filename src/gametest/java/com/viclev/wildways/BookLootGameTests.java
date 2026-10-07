package com.viclev.wildways;

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
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

/** Exercises the loaded loot tables, including existing Bastion rewards. */
public class BookLootGameTests {
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
