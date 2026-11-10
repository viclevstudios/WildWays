package com.viclev.wildways;

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
			quest("shell", ModItems.ENDERMITE_SHELL, null),
			quest("nest", ModBlocks.ENDERMITE_BOX, "shell"),
			quest("bricks", ModBlocks.ENDERMITE_BRICKS, "shell")
		)),
		new Group("food", Items.BREAD, List.of(
			quest("bait", ModItems.BEETROOT_DOUGH, null),
			quest("rich_fish", Items.COOKED_SALMON, "bait")
		)),
		new Group("exploration", Items.COMPASS, List.of(
			quest("compass", Items.COMPASS, null),
			quest("biome_compass", ModItems.BIOME_COMPASS, "compass")
		)),
		new Group("equipment", Items.IRON_PICKAXE, List.of(
			quest("quartz_tool", ProgressionItems.QUARTZ_PICKAXE, null),
			quest("volcanite_tool", ModItems.VOLCANITE_PICKAXE, "quartz_tool")
		)),
		new Group("enchanting", Items.ENCHANTING_TABLE, List.of(
			quest("rune_enchanting", EnchantingItems.RUNES.getFirst(), null),
			quest("mending", Items.ANVIL, "rune_enchanting")
		)),
		new Group("brewing", Items.BREWING_STAND, List.of(
			quest("unease", Items.POTION, null),
			quest("brewing_eye", ModItems.EYE_OF_BREWING, "unease")
		)),
		new Group("end", Items.END_PORTAL_FRAME, List.of(
			quest("portal_eye", ModItems.EYE_OF_ICE, null),
			new Quest("enter_end", Items.END_STONE, "portal_eye", Identifier.withDefaultNamespace("end/root"))
		)),
		new Group("wither", Items.WITHER_SKELETON_SKULL, List.of(
			new Quest("summon_wither", Items.SOUL_SAND, null, Identifier.withDefaultNamespace("nether/summon_wither")),
			quest("defeat_wither", Items.NETHER_STAR, "summon_wither")
		))
	);

	private QuestGuide() {
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
