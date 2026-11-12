package com.viclev.wildways;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/** Guide layout and text keys. The visual parent has no effect on advancement criteria. */
public final class QuestGuide {
	public static final List<Group> GROUPS = List.of(
		new Group("endermites", ModBlocks.ENDERMITE_BOX.asItem(), List.of(
			quest("quarantine_grounds", Items.MOSSY_STONE_BRICKS, null),
			quest("kill_endermite", Items.ENDER_PEARL, null),
			quest("shell", ModItems.ENDERMITE_SHELL, "kill_endermite"),
			quest("nest", ModBlocks.ENDERMITE_BOX, "shell"),
			quest("packed_nest", Items.CHEST, "nest"),
			quest("endermite_eye", ModItems.EYE_OF_ENDERMITES, "nest"),
			quest("bricks", ModBlocks.ENDERMITE_BRICKS, "shell"),
			quest("brew_unease", Items.POTION, "shell")
		)),
		new Group("food", Items.BREAD, foodQuests()),
		new Group("exploration", Items.COMPASS, List.of(
			quest("biome_compass", ModItems.BIOME_COMPASS, null),
			quest("locate_biome", Items.SPYGLASS, "biome_compass"),
			quest("structure_map", Items.FILLED_MAP, null),
			quest("trade_structure_map", Items.EMERALD, "structure_map"),
			quest("igloo_map", ModItems.IGLOO_MAP, "structure_map"),
			quest("dungeon_map", ModItems.DUNGEON_MAP, "structure_map"),
			quest("mineshaft_map", Items.BURIED_MINESHAFT_MAP, "structure_map"),
			quest("trail_ruins_map", ModItems.TRAIL_RUINS_MAP, "structure_map"),
			quest("pillager_outpost_map", ModItems.PILLAGER_OUTPOST_MAP, "structure_map"),
			quest("jungle_temple_map", Items.JUNGLE_PYRAMID_MAP, "structure_map"),
			quest("desert_temple_map", Items.DESERT_PYRAMID_MAP, "structure_map"),
			quest("swamp_hut_map", Items.SWAMP_HUT_MAP, "structure_map"),
			quest("ancient_city_map", Items.BURIED_ANCIENT_CITY_MAP, "structure_map"),
			quest("land_with_worn_elytra", Items.ELYTRA, null),
			quest("read_light_sensor", ModBlocks.LIGHT_SENSOR, null),
			quest("compass", Items.COMPASS, null),
			quest("read_clock", Items.CLOCK, null)
		)),
		new Group("equipment", Items.IRON_PICKAXE, List.of(
			quest("mine_with_copper", Items.COPPER_PICKAXE, null),
			quest("refine_iron", Items.SMITHING_TABLE, null),
			quest("craft_gold_refinement", ProgressionItems.REFINEMENT_GOLD, "refine_iron"),
			quest("refine_gold", Items.GOLDEN_PICKAXE, "craft_gold_refinement"),
			quest("craft_amethyst_refinement", ProgressionItems.REFINEMENT_AMETHYST, "refine_iron"),
			quest("amethyst_combat", ProgressionItems.AMETHYST_SWORD, "craft_amethyst_refinement"),
			quest("wear_amethyst_armor", ProgressionItems.AMETHYST_CHESTPLATE, "craft_amethyst_refinement"),
			quest("craft_quartz_refinement", ProgressionItems.REFINEMENT_QUARTZ, "refine_iron"),
			quest("quartz_tool", ProgressionItems.QUARTZ_PICKAXE, "craft_quartz_refinement"),
			quest("wear_quartz_armor", ProgressionItems.QUARTZ_CHESTPLATE, "craft_quartz_refinement"),
			quest("find_raw_volcanite", ModItems.RAW_VOLCANITE, null),
			quest("craft_volcanite_refinement", ProgressionItems.REFINEMENT_VOLCANITE, "find_raw_volcanite"),
			quest("volcanite_tool", ModItems.VOLCANITE_PICKAXE, "craft_volcanite_refinement"),
			quest("wear_volcanite_armor", ModItems.VOLCANITE_CHESTPLATE, "craft_volcanite_refinement"),
			quest("craft_diamond_refinement", ProgressionItems.REFINEMENT_DIAMOND, null),
			quest("refine_diamond", Items.DIAMOND_PICKAXE, "craft_diamond_refinement"),
			quest("fletch_arrows", Items.FLETCHING_TABLE, null),
			quest("fletch_turtle_arrow", ModItems.TURTLE_ARROW, "fletch_arrows"),
			quest("fletch_range_arrow", ModItems.RANGE_ARROW, "fletch_arrows"),
			quest("fletch_explosive_arrow", ModItems.EXPLOSIVE_ARROW, "fletch_arrows")
		)),
		new Group("enchanting", Items.ENCHANTING_TABLE, List.of(
			quest("craft_rune", EnchantingItems.RUNES.getFirst(), null),
			quest("rune_enchanting", Items.ENCHANTING_TABLE, "craft_rune"),
			quest("enchant_level_five", EnchantingItems.RUNES.getLast(), "rune_enchanting"),
			quest("craft_enchanting_catalyst", Items.SCULK_CATALYST, "rune_enchanting"),
			quest("use_enchanting_catalyst", EnchantingItems.CATALYST, "craft_enchanting_catalyst"),
			quest("mending", Items.ENCHANTED_BOOK, null),
			quest("repair_mending_item", Items.ANVIL, "mending")
		)),
		new Group("brewing", Items.BREWING_STAND, List.of(
			quest("stack_potions", Items.GLASS_BOTTLE, null),
			quest("brew_level_two", Items.GLOWSTONE_DUST, null),
			quest("brew_supreme", Items.POPPED_CHORUS_FRUIT, "brew_level_two"),
			quest("drink_supreme", Items.POTION, "brew_supreme"),
			quest("brew_fatal", Items.FERMENTED_SPIDER_EYE, "brew_level_two"),
			quest("use_fatal", Items.SPLASH_POTION, "brew_fatal"),
			quest("tip_eight_arrows", Items.TIPPED_ARROW, null),
			quest("tip_stack_arrows", Items.LINGERING_POTION, "tip_eight_arrows")
		)),
		new Group("end", Items.END_PORTAL_FRAME, List.of(
			quest("eye_of_ice", ModItems.EYE_OF_ICE, null),
			quest("eye_of_water", ModItems.EYE_OF_WATER, null),
			quest("eye_of_sulfur", ModItems.EYE_OF_SULFUR, null),
			quest("eye_of_the_tiger", ModItems.EYE_OF_THE_TIGER, null),
			quest("lost_eye", ModItems.LOST_EYE, null),
			quest("eye_of_storm", ModItems.EYE_OF_STORM, null),
			quest("endermite_eye", ModItems.EYE_OF_ENDERMITES, null),
			quest("eye_of_the_brute", ModItems.EYE_OF_THE_BRUTE, null),
			quest("eye_of_the_creaking", ModItems.EYE_OF_THE_CREAKING, null),
			new Quest("eye_of_brewing", ModItems.EYE_OF_BREWING, null, Wildways.id("quests/brewing_eye")),
			quest("enchanted_eye", ModItems.ENCHANTED_EYE, null),
			quest("eye_of_volcanite", ModItems.EYE_OF_VOLCANITE, null),
			quest("eye_of_illagers", ModItems.EYE_OF_ILLAGERS, null),
			quest("eye_of_darkness", ModItems.EYE_OF_DARKNESS, null),
			quest("collect_portal_eyes", Items.ENDER_EYE, null),
			quest("activate_end_portal", Items.END_PORTAL_FRAME, "collect_portal_eyes")
		)),
		new Group("wither", Items.WITHER_SKELETON_SKULL, List.of(
			quest("find_wither_skull", Items.WITHER_SKELETON_SKULL, null),
			quest("find_sculk_wither_skull", WitherSkulls.SCULK.item(), null),
			quest("find_gold_wither_skull", WitherSkulls.GOLD.item(), null),
			quest("summon_wither", Items.SOUL_SAND, "find_wither_skull"),
			quest("defeat_wither", Items.NETHER_STAR, "summon_wither")
		))
	);

	private QuestGuide() {
	}

	private static List<Quest> foodQuests() {
		var quests = new ArrayList<>(List.of(
			quest("plant_seed", Items.WHEAT_SEEDS, null),
			quest("till_fast_farmland", Items.DIAMOND_HOE, "plant_seed"),
			quest("till_netherite_farmland", Items.NETHERITE_HOE, "till_fast_farmland"),
			quest("find_cycad", PlantBlocks.CYCAD, "plant_seed"),
			quest("compost_cycad", Items.COMPOSTER, "find_cycad"),
			quest("till_compost", PlantBlocks.COMPOST_ITEM, "compost_cycad"),
			quest("find_sniffer_flowers", Items.TORCHFLOWER_SEEDS, "plant_seed"),
			quest("pitcher_irrigation", Items.PITCHER_POD, "find_sniffer_flowers"),
			quest("torchflower_light", Items.TORCHFLOWER, "find_sniffer_flowers"),
			quest("gold_ripen", Items.WHEAT, "find_sniffer_flowers"),
			quest("gold_ripen_compost", Items.GOLDEN_CARROT, "gold_ripen"),
			quest("eat_gold_ripened_stew", Items.RABBIT_STEW, "gold_ripen"),
			quest("craft_fishing_rod", Items.FISHING_ROD, null),
			quest("bait", ModItems.BEETROOT_DOUGH, "craft_fishing_rod"),
			quest("rich_fish", Items.COOKED_SALMON, "bait")
		));
		if (AnimalWeightsQuests.isAvailable()) {
			quests.addAll(List.of(
				quest("hunt_livestock", Items.BEEF, null),
				quest("craft_magnifying_glass", Items.SPYGLASS, "hunt_livestock"),
				quest("perfect_habitat", Items.GRASS_BLOCK, "hunt_livestock"),
				quest("hunt_heavy_livestock", Items.COOKED_BEEF, "hunt_livestock")
			));
		}
		return List.copyOf(quests);
	}

	private static Quest quest(String id, ItemLike icon, String parent) {
		return new Quest(id, icon.asItem(), parent, Wildways.id("quests/" + id));
	}

	public record Group(String id, Item icon, List<Quest> quests) {
		public Component title() {
			return Component.translatable("wildways.guide.group." + this.id);
		}
	}

	public record Quest(String id, Item icon, String parent, Identifier advancement) {
		public Component title() {
			return Component.translatable("wildways.guide.quest." + this.id);
		}

		public Component description() {
			return Component.translatable("wildways.guide.quest." + this.id + ".description");
		}
	}

	public record Progress(int completed, int total) {
		public static final Progress UNAVAILABLE = new Progress(0, 0);
		private static final StreamCodec<RegistryFriendlyByteBuf, Progress> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, Progress::completed,
			ByteBufCodecs.VAR_INT, Progress::total,
			Progress::new
		);

		public boolean done() {
			return this.total > 0 && this.completed == this.total;
		}
	}

	public record Open(Map<Identifier, Progress> progress) implements CustomPacketPayload {
		public static final Type<Open> TYPE = new Type<>(Wildways.id("open_quest_guide"));
		static final StreamCodec<RegistryFriendlyByteBuf, Open> CODEC = StreamCodec.composite(
			ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, Progress.CODEC, 256), Open::progress,
			Open::new
		);

		public Open {
			progress = Map.copyOf(progress);
		}

		@Override
		public Type<Open> type() {
			return TYPE;
		}
	}
}
