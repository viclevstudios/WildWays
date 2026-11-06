package com.viclev.wildways;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Unit;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jspecify.annotations.Nullable;

public record FishingBait(Type type, int charges) {
	public static final int MAX_CHARGES = 16;
	private static final Codec<FishingBait> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Type.CODEC.fieldOf("type").forGetter(FishingBait::type),
		Codec.intRange(1, MAX_CHARGES).fieldOf("charges").forGetter(FishingBait::charges)
	).apply(instance, FishingBait::new));
	public static final DataComponentType<FishingBait> LOADED_BAIT = DataComponentType.<FishingBait>builder().persistent(CODEC).build();
	public static final DataComponentType<Unit> RICH_FISH = DataComponentType.<Unit>builder().persistent(Unit.CODEC).build();

	public static void initialize() {
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Wildways.id("fishing_bait"), LOADED_BAIT);
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Wildways.id("rich_fish"), RICH_FISH);
	}

	public static @Nullable Type consume(ItemStack rod) {
		FishingBait bait = rod.get(LOADED_BAIT);
		if (bait == null) {
			return null;
		}
		if (bait.charges == 1) {
			rod.remove(LOADED_BAIT);
		} else {
			rod.set(LOADED_BAIT, new FishingBait(bait.type, bait.charges - 1));
		}
		return bait.type;
	}

	public static ObjectArrayList<ItemStack> catchLoot(Type bait, LootTable vanillaTable, LootParams params,
		boolean openWater, RandomSource random) {
		LootChances chances = LootChances.forBait(bait, openWater, params.getLuck());
		if (chances == null) {
			return vanillaTable.getRandomItems(params);
		}
		float roll = random.nextFloat();
		ResourceKey<LootTable> table;
		if (roll < chances.treasure) {
			table = BuiltInLootTables.FISHING_TREASURE;
		} else if (roll < chances.treasure + chances.junk) {
			table = BuiltInLootTables.FISHING_JUNK;
		} else {
			table = BuiltInLootTables.FISHING_FISH;
		}
		ObjectArrayList<ItemStack> loot = params.getLevel().getServer().reloadableRegistries().getLootTable(table).getRandomItems(params);
		if (bait == Type.BEETROOT_DOUGH && table.equals(BuiltInLootTables.FISHING_FISH)) {
			loot.forEach(FishingBait::enrichFish);
		}
		return loot;
	}

	public static void enrichFish(ItemStack fish) {
		boolean rich = fish.has(RICH_FISH);
		if (fish.is(Items.PUFFERFISH)) {
			if (rich) {
				fish.remove(RICH_FISH);
				fish.set(DataComponents.FOOD, fish.getPrototype().get(DataComponents.FOOD));
			}
			return;
		}
		FoodProperties food = fish.get(DataComponents.FOOD);
		if (food == null || (!rich && !fish.is(ItemTags.FISHES))) {
			return;
		}
		// The food component uses two nutrition points per hunger icon.
		int nutrition = switch (fish.getItem()) {
			case Item item when item == Items.COD || item == Items.SALMON -> 3;
			case Item item when item == Items.TROPICAL_FISH -> 2;
			case Item item when item == Items.COOKED_COD -> 7;
			case Item item when item == Items.COOKED_SALMON -> 8;
			default -> food.nutrition();
		};
		if (!rich || nutrition != food.nutrition()) {
			float saturation = rich ? food.saturation() : food.saturation() * 1.3F;
			fish.set(DataComponents.FOOD, new FoodProperties(nutrition, saturation, food.canAlwaysEat()));
			fish.set(RICH_FISH, Unit.INSTANCE);
		}
	}

	public enum Type implements StringRepresentable {
		BEETROOT_DOUGH("beetroot_dough"),
		CANDIED_BAIT("candied_bait"),
		AMETHYST_BLINKER("amethyst_blinker");

		static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);
		private final String id;

		Type(String id) {
			this.id = id;
		}

		@Override
		public String getSerializedName() {
			return this.id;
		}

		public Component nameComponent() {
			return Component.translatable(Wildways.id(this.id).toLanguageKey("item"));
		}
	}

	public record LootChances(float junk, float treasure) {
		public static @Nullable LootChances forBait(Type bait, boolean openWater, float luck) {
			// Match the category weights and luck qualities in the vanilla fishing table.
			int fishWeight = Math.max(Mth.floor(85 - luck), 0);
			int junkWeight = Math.max(Mth.floor(10 - 2 * luck), 0);
			int treasureWeight = openWater ? Math.max(Mth.floor(5 + 2 * luck), 0) : 0;
			float totalWeight = (float) fishWeight + junkWeight + treasureWeight;
			if (totalWeight == 0) {
				return null;
			}
			float junk = junkWeight / totalWeight;
			float treasure = treasureWeight / totalWeight;
			return switch (bait) {
				case BEETROOT_DOUGH -> new LootChances(Math.max(0, junk - 0.05F), treasure);
				case CANDIED_BAIT -> new LootChances(junk * 0.5F, treasure);
				case AMETHYST_BLINKER -> {
					float doubledTreasure = Math.min(1, treasure * 2);
					yield new LootChances(Math.min(junk, 1 - doubledTreasure), doubledTreasure);
				}
			};
		}
	}
}
