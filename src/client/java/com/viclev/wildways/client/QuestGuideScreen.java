package com.viclev.wildways.client;

import com.viclev.wildways.ModItems;
import com.viclev.wildways.QuestGuide;
import com.viclev.wildways.QuestGuide.Group;
import com.viclev.wildways.QuestGuide.Progress;
import com.viclev.wildways.QuestGuide.Quest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

final class QuestGuideScreen extends Screen {
	private static final Component TITLE = message("title");
	private static final int TEXT = 0xFFF8F7F2;
	private static final int SECONDARY = 0xFFDDDFDD;
	private static final int COMPLETE = 0xFFB6EF92;
	private static final int LINE = 0xFFF4F4F4;
	private static final int HEADER = 36;
	private static final int FOOTER = 25;
	private static final int NODE_RADIUS = 14;
	private static final int ROOT_RADIUS = 17;

	private final Map<Identifier, Progress> progress;
	private final Group group;
	private final Quest quest;
	private final List<Link> links = new ArrayList<>();
	private Point center;

	private QuestGuideScreen(Map<Identifier, Progress> progress, Group group, Quest quest) {
		super(quest != null ? quest.title() : group != null ? group.title() : TITLE);
		this.progress = progress;
		this.group = group;
		this.quest = quest;
	}

	static void initialize() {
		ClientPlayNetworking.registerGlobalReceiver(QuestGuide.Open.TYPE, (open, context) -> {
			var client = context.client();
			if (client.gui.screen() == null && client.gui.overlay() == null) {
				client.gui.setScreen(new QuestGuideScreen(open.progress(), null, null));
			}
		});
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		if (this.group != null) {
			this.minecraft.gui.setScreen(new QuestGuideScreen(this.progress, this.quest == null ? null : this.group, null));
		} else {
			super.onClose();
		}
	}

	@Override
	protected void init() {
		this.center = new Point(this.width / 2, (HEADER + this.height - FOOTER) / 2);
		this.links.clear();
		if (this.group != null) {
			this.addRenderableWidget(Button.builder(message("back"), button -> this.onClose())
				.bounds(10, 8, 64, 20).build());
		}
		this.addRenderableWidget(Button.builder(message("close"), button -> super.onClose())
			.bounds(this.width - 74, 8, 64, 20).build());
		if (this.group == null) {
			this.layoutOverview();
		} else if (this.quest == null) {
			switch (this.group.id()) {
				case "endermites" -> this.layoutParasiteChapter();
				case "food" -> this.layoutFoodChapter();
				case "end" -> this.layoutCollectorChapter();
				case "exploration" -> this.layoutExplorationChapter();
				case "brewing" -> this.layoutBrewingChapter();
				case "equipment" -> this.layoutEquipmentChapter();
				case "enchanting" -> this.layoutEnchantingChapter();
				case "wither" -> this.layoutWitherChapter();
				default -> this.layoutBranches(null, this.center, 1, this.branchDepth(null), 0, Math.TAU);
			}
		}
	}

	private void layoutWitherChapter() {
		int column = Math.min(104, (this.width / 2 - 20) / 3);
		int row = Math.min(76, (this.height - HEADER - FOOTER) / 2 - 20);
		var positions = Map.of(
			"find_wither_skull", this.center.shift(-column, 0),
			"find_sculk_wither_skull", this.center.shift(-column, -row),
			"find_gold_wither_skull", this.center.shift(-column, row),
			"summon_wither", this.center.shift(-2 * column, 0),
			"defeat_wither", this.center.shift(-3 * column, 0)
		);
		this.layoutChapter(positions, id -> Label.HIDDEN);
		Quest summon = this.branches("find_wither_skull").getFirst();
		for (String id : List.of("find_sculk_wither_skull", "find_gold_wither_skull")) {
			this.links.add(new Link(positions.get(id), positions.get(summon.id()), this.progress(summon).done()));
		}
	}

	private void layoutFoodChapter() {
		int column = Math.min(70, (this.width / 2 - 20) / 3);
		int row = Math.min(56, ((this.height - HEADER - FOOTER) / 2 - 18) / 2);
		this.layoutChapter(Map.ofEntries(
			Map.entry("plant_seed", this.center.shift(-column, 0)),
			Map.entry("till_fast_farmland", this.center.shift(-2 * column, -2 * row)),
			Map.entry("till_netherite_farmland", this.center.shift(-3 * column, -2 * row)),
			Map.entry("find_cycad", this.center.shift(-2 * column, 0)),
			Map.entry("compost_cycad", this.center.shift(-3 * column, -row)),
			Map.entry("till_compost", this.center.shift(-3 * column, 0)),
			Map.entry("find_sniffer_flowers", this.center.shift(-column, row)),
			Map.entry("pitcher_irrigation", this.center.shift(-3 * column, 2 * row)),
			Map.entry("torchflower_light", this.center.shift(-2 * column, 2 * row)),
			Map.entry("gold_ripen", this.center.shift(0, 2 * row)),
			Map.entry("gold_ripen_compost", this.center.shift(column, row)),
			Map.entry("eat_gold_ripened_stew", this.center.shift(2 * column, 2 * row)),
			Map.entry("craft_fishing_rod", this.center.shift(column, -row)),
			Map.entry("bait", this.center.shift(2 * column, -2 * row)),
			Map.entry("rich_fish", this.center.shift(3 * column, -2 * row)),
			Map.entry("hunt_livestock", this.center.shift(column, 0)),
			Map.entry("craft_magnifying_glass", this.center.shift(2 * column, -row)),
			Map.entry("perfect_habitat", this.center.shift(3 * column, 0)),
			Map.entry("hunt_heavy_livestock", this.center.shift(2 * column, row))
		), id -> Label.HIDDEN);
	}

	private void layoutExplorationChapter() {
		int horizontal = Math.min(190, this.width / 2 - NODE_RADIUS - 4);
		int vertical = Math.min(120, (this.height - HEADER - FOOTER) / 2 - NODE_RADIUS - 4);
		Map<String, Point> roots = Map.of(
			"biome_compass", this.center.shift(-horizontal / 2, 0),
			"locate_biome", this.center.shift(-horizontal, 0),
			"land_with_worn_elytra", this.center.shift(-horizontal, -vertical),
			"read_light_sensor", this.center.shift(-horizontal / 3, -vertical),
			"compass", this.center.shift(-horizontal / 3, vertical),
			"read_clock", this.center.shift(-horizontal, vertical),
			"structure_map", this.center.shift(horizontal / 3, 0)
		);
		for (Quest entry : this.branches(null)) {
			Point point = roots.get(entry.id());
			this.addQuestNode(entry, this.center, point, Label.HIDDEN);
			List<Quest> children = this.branches(entry.id());
			if (entry.id().equals("structure_map")) {
				double spread = Math.PI * 1.2;
				for (int i = 0; i < children.size(); i++) {
					double angle = (i + 0.5) * spread / children.size() - spread / 2;
					Point child = point.offset(horizontal * 2 / 3, vertical, angle);
					this.addQuestNode(children.get(i), point, child, Label.HIDDEN, 10);
				}
			} else {
				for (Quest child : children) {
					this.addQuestNode(child, point, roots.get(child.id()), Label.HIDDEN);
				}
			}
		}
	}

	private void layoutCollectorChapter() {
		List<Quest> branches = this.branches(null);
		int horizontal = Math.min(190, this.width / 2 - 50);
		int vertical = Math.min(124, (this.height - HEADER - FOOTER) / 2 - 16);
		for (int i = 0; i < branches.size(); i++) {
			Quest entry = branches.get(i);
			double angle = (i + 1) * Math.TAU / branches.size();
			Point point = this.center.offset(horizontal, vertical, angle);
			this.addQuestNode(entry, this.center, point, Label.HIDDEN);
			for (Quest child : this.branches(entry.id())) {
				this.addQuestNode(child, point, point.shift(32, 0), Label.HIDDEN);
			}
		}
	}

	private void layoutParasiteChapter() {
		int x = Math.min(200, this.width / 2 - 20);
		int y = Math.min(92, (this.height - HEADER - FOOTER) / 2 - 32);
		Map<String, Point> positions = Map.of(
			"quarantine_grounds", this.center.shift(-x * 3 / 4, 0),
			"kill_endermite", this.center.shift(x / 3, 0),
			"shell", this.center.shift(x * 3 / 4, 0),
			"nest", this.center.shift(x * 3 / 5, -y),
			"packed_nest", this.center.shift(-x / 4, -y),
			"endermite_eye", this.center.shift(x, -y),
			"bricks", this.center.shift(x / 7, y),
			"brew_unease", this.center.shift(x * 9 / 10, y)
		);
		this.layoutChapter(positions, id -> switch (id) {
			case "nest", "packed_nest" -> Label.ABOVE;
			case "shell" -> Label.RIGHT;
			default -> Label.BELOW;
		});
	}

	private void layoutBrewingChapter() {
		int x = Math.min(200, this.width / 2 - 18);
		int y = Math.min(90, (this.height - HEADER - FOOTER) / 2 - 32);
		this.layoutChapter(Map.of(
			"stack_potions", this.center.shift(-x * 2 / 3, -y),
			"brew_level_two", this.center.shift(x / 3, 0),
			"brew_supreme", this.center.shift(x * 2 / 3, -y),
			"drink_supreme", this.center.shift(x, -y),
			"brew_fatal", this.center.shift(x * 2 / 3, y),
			"use_fatal", this.center.shift(x, y),
			"tip_eight_arrows", this.center.shift(-x / 3, y),
			"tip_stack_arrows", this.center.shift(-x * 4 / 5, y)
		), id -> switch (id) {
			case "stack_potions", "brew_level_two", "brew_supreme", "use_fatal", "tip_stack_arrows" -> Label.ABOVE;
			default -> Label.BELOW;
		});
	}

	private void layoutChapter(Map<String, Point> positions, Function<String, Label> labels) {
		for (Quest entry : this.group.quests()) {
			Point from = entry.parent() == null ? this.center : positions.get(entry.parent());
			Point point = positions.get(entry.id());
			this.addQuestNode(entry, from, point, labels.apply(entry.id()));
		}
	}

	private void layoutEnchantingChapter() {
		int x = Math.min(200, this.width / 2 - 20);
		int y = Math.min(90, (this.height - HEADER - FOOTER) / 2 - 32);
		this.layoutChapter(Map.of(
			"craft_rune", this.center.shift(x * 3 / 10, 0),
			"rune_enchanting", this.center.shift(x * 3 / 5, 0),
			"enchant_level_five", this.center.shift(x, -y),
			"craft_enchanting_catalyst", this.center.shift(x, y),
			"use_enchanting_catalyst", this.center.shift(x * 3 / 7, y),
			"mending", this.center.shift(-x * 2 / 5, 0),
			"repair_mending_item", this.center.shift(-x * 4 / 5, 0)
		), id -> switch (id) {
			case "craft_rune", "craft_enchanting_catalyst", "repair_mending_item" -> Label.BELOW;
			default -> Label.ABOVE;
		});
	}

	private void layoutEquipmentChapter() {
		int column = Math.min(74, (this.width / 2 - 22) / 3);
		int row = Math.min(58, ((this.height - HEADER - FOOTER) / 2 - 18) / 2);
		this.layoutChapter(Map.ofEntries(
			Map.entry("mine_with_copper", this.center.shift(0, -2 * row)),
			Map.entry("refine_iron", this.center.shift(-column, 0)),
			Map.entry("craft_gold_refinement", this.center.shift(-2 * column, -2 * row)),
			Map.entry("refine_gold", this.center.shift(-3 * column, -2 * row)),
			Map.entry("craft_amethyst_refinement", this.center.shift(-2 * column, 0)),
			Map.entry("amethyst_combat", this.center.shift(-3 * column, -row)),
			Map.entry("wear_amethyst_armor", this.center.shift(-3 * column, 0)),
			Map.entry("craft_quartz_refinement", this.center.shift(-2 * column, 2 * row)),
			Map.entry("quartz_tool", this.center.shift(-3 * column, row)),
			Map.entry("wear_quartz_armor", this.center.shift(-3 * column, 2 * row)),
			Map.entry("find_raw_volcanite", this.center.shift(column, -row)),
			Map.entry("craft_volcanite_refinement", this.center.shift(2 * column, -row)),
			Map.entry("volcanite_tool", this.center.shift(3 * column, -2 * row)),
			Map.entry("wear_volcanite_armor", this.center.shift(3 * column, -row)),
			Map.entry("craft_diamond_refinement", this.center.shift(column, 0)),
			Map.entry("refine_diamond", this.center.shift(3 * column, 0)),
			Map.entry("fletch_arrows", this.center.shift(column, row)),
			Map.entry("fletch_turtle_arrow", this.center.shift(3 * column, row)),
			Map.entry("fletch_range_arrow", this.center.shift(3 * column, 2 * row)),
			Map.entry("fletch_explosive_arrow", this.center.shift(column, 2 * row))
		), id -> Label.HIDDEN);
	}

	private void layoutOverview() {
		int radius = Math.max(28, Math.min(112, Math.min((this.height - 128) / 2, (this.width - 178) / 2)));
		int groupCount = QuestGuide.GROUPS.size();
		for (int i = 0; i < groupCount; i++) {
			Group entry = QuestGuide.GROUPS.get(i);
			double angle = -Math.PI / 2 + i * Math.TAU / groupCount;
			Point point = this.center.offset(radius, radius, angle);
			int completed = this.completedQuests(entry);
			boolean done = completed == entry.quests().size();
			Label label = Math.abs(Math.cos(angle)) < 0.1
				? (point.y < this.center.y ? Label.ABOVE : Label.BELOW)
				: (point.x < this.center.x ? Label.LEFT : Label.RIGHT);
			this.links.add(new Link(this.center, point, done));
			this.addRenderableWidget(new NodeButton(point, NODE_RADIUS, entry.icon().getDefaultInstance(), entry.title(),
				message("group_progress", completed, entry.quests().size()), label, done,
				() -> this.minecraft.gui.setScreen(new QuestGuideScreen(this.progress, entry, null))));
		}
	}

	private List<Quest> branches(String parent) {
		return this.group.quests().stream().filter(entry -> Objects.equals(entry.parent(), parent)).toList();
	}

	private int branchDepth(String parent) {
		return this.branches(parent).stream().mapToInt(entry -> 1 + this.branchDepth(entry.id())).max().orElse(0);
	}

	private void layoutBranches(String parent, Point from, int depth, int maxDepth, double heading, double spread) {
		List<Quest> branches = this.branches(parent);
		if (branches.isEmpty()) {
			return;
		}
		double spacing = spread / branches.size();
		int horizontal = Math.min(192, this.width / 2 - 46) * depth / maxDepth;
		int vertical = Math.min(130, (this.height - HEADER - FOOTER) / 2 - 30) * depth / maxDepth;
		for (int i = 0; i < branches.size(); i++) {
			Quest entry = branches.get(i);
			double angle = heading + (i - (branches.size() - 1) / 2.0) * spacing;
			Point point = this.center.offset(horizontal, vertical, angle);
			Label label = point.y < this.center.y || (point.y == this.center.y && depth % 2 == 1)
				? Label.ABOVE : Label.BELOW;
			this.addQuestNode(entry, from, point, label);
			this.layoutBranches(entry.id(), point, depth + 1, maxDepth, angle, Math.min(spacing, Math.PI * 0.7));
		}
	}

	private void addQuestNode(Quest entry, Point from, Point point, Label label) {
		this.addQuestNode(entry, from, point, label, NODE_RADIUS);
	}

	private void addQuestNode(Quest entry, Point from, Point point, Label label, int radius) {
		Progress state = this.progress(entry);
		this.links.add(new Link(from, point, state.done()));
		this.addRenderableWidget(new NodeButton(point, radius, entry.icon().getDefaultInstance(), entry.title(),
			status(state), label, state.done(),
			() -> this.minecraft.gui.setScreen(new QuestGuideScreen(this.progress, this.group, entry))));
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		graphics.fill(0, 0, this.width, this.height, 0xCD3C3E40);
		graphics.fill(0, 0, this.width, HEADER, 0x90202122);
		graphics.fill(0, this.height - FOOTER, this.width, this.height, 0x90202122);
		graphics.fill(0, HEADER, this.width, HEADER + 1, 0x508F9497);
		graphics.fill(0, this.height - FOOTER, this.width, this.height - FOOTER + 1, 0x508F9497);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		Component heading = this.group == null ? TITLE : this.group.title();
		graphics.centeredText(this.font, heading.copy().withStyle(ChatFormatting.BOLD), this.width / 2, 14, TEXT);
		if (this.quest == null) {
			for (Link link : this.links) {
				line(graphics, link.from, link.to, 1, link.done ? COMPLETE : LINE);
			}
			ItemStack icon = (this.group == null ? ModItems.QUEST_BOOK : this.group.icon()).getDefaultInstance();
			boolean done = this.group != null && this.completedQuests(this.group) == this.group.quests().size();
			drawNode(graphics, this.center, ROOT_RADIUS, icon, done, false);
			if (this.group != null && switch (this.group.id()) {
				case "endermites", "food", "end", "exploration", "brewing", "equipment", "enchanting" -> false;
				default -> true;
			}) {
				this.drawLabel(graphics, this.center, ROOT_RADIUS, this.group.title(), Label.BELOW, TEXT);
			}
		} else {
			this.drawQuest(graphics);
		}
		graphics.centeredText(this.font, this.footer(), this.width / 2, this.height - 16, SECONDARY);
		super.extractRenderState(graphics, mouseX, mouseY, delta);
	}

	private Component footer() {
		if (this.quest != null) {
			return message("detail_hint");
		}
		if (this.group != null) {
			return message("branch_hint", this.completedQuests(this.group), this.group.quests().size());
		}
		int completed = QuestGuide.GROUPS.stream().mapToInt(this::completedQuests).sum();
		int total = QuestGuide.GROUPS.stream().mapToInt(entry -> entry.quests().size()).sum();
		return message("overview_hint", completed, total);
	}

	private void drawQuest(GuiGraphicsExtractor graphics) {
		int panelWidth = Math.min(344, this.width - 32);
		int left = (this.width - panelWidth) / 2;
		var description = this.font.split(this.quest.description(), panelWidth - 32);
		int panelHeight = 86 + description.size() * 12;
		int top = this.center.y - panelHeight / 2;
		Progress state = this.progress(this.quest);
		graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xDC282A2C);
		graphics.fill(left, top, left + panelWidth, top + 1, 0xFF8F9497);
		drawNode(graphics, new Point(left + 30, top + 29), NODE_RADIUS, this.quest.icon().getDefaultInstance(), state.done(), false);
		graphics.text(this.font, this.quest.title().copy().withStyle(ChatFormatting.BOLD), left + 52, top + 25, TEXT);
		int y = top + 53;
		for (var row : description) {
			graphics.text(this.font, row, left + 16, y, SECONDARY);
			y += 12;
		}
		graphics.fill(left + 16, y + 2, left + panelWidth - 16, y + 3, 0xFF595D60);
		graphics.text(this.font, status(state), left + 16, y + 12, state.done() ? COMPLETE : TEXT);
	}

	private void drawLabel(GuiGraphicsExtractor graphics, Point point, int radius, Component title, Label label, int color) {
		int textWidth = this.font.width(title);
		int x = switch (label) {
			case LEFT -> point.x - radius - 5 - textWidth;
			case RIGHT -> point.x + radius + 5;
			default -> point.x - textWidth / 2;
		};
		int y = switch (label) {
			case ABOVE -> point.y - radius - 13;
			case BELOW -> point.y + radius + 5;
			default -> point.y - 4;
		};
		graphics.text(this.font, title, Math.max(5, Math.min(this.width - 5 - textWidth, x)), y, color);
	}

	private static void drawNode(GuiGraphicsExtractor graphics, Point point, int radius, ItemStack icon, boolean done, boolean highlighted) {
		int outline = done ? COMPLETE : highlighted ? LINE : 0xFFA6ACAF;
		octagon(graphics, point, radius, outline);
		octagon(graphics, point, radius - (highlighted ? 2 : 1), highlighted ? 0xFF474D50 : 0xFF303436);
		graphics.item(icon, point.x - 8, point.y - 8);
		if (done) {
			Point badge = new Point(point.x + radius - 3, point.y - radius + 3);
			octagon(graphics, badge, 6, 0xFF263825);
			Point bend = new Point(badge.x - 1, badge.y + 2);
			line(graphics, new Point(badge.x - 4, badge.y - 1), bend, 2, COMPLETE);
			line(graphics, bend, new Point(badge.x + 4, badge.y - 3), 2, COMPLETE);
		}
	}

	private static void octagon(GuiGraphicsExtractor graphics, Point point, int radius, int color) {
		int corner = radius / 3;
		for (int row = -corner; row < 0; row++) {
			graphics.fill(point.x - radius - row, point.y - radius + row + corner,
				point.x + radius + row, point.y - radius + row + corner + 1, color);
			graphics.fill(point.x - radius - row, point.y + radius - row - corner - 1,
				point.x + radius + row, point.y + radius - row - corner, color);
		}
		graphics.fill(point.x - radius, point.y - radius + corner, point.x + radius, point.y + radius - corner, color);
	}

	private static void line(GuiGraphicsExtractor graphics, Point from, Point to, int thickness, int color) {
		int dx = to.x - from.x;
		int dy = to.y - from.y;
		var pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(from.x, from.y);
		pose.rotate((float) Math.atan2(dy, dx));
		graphics.fill(0, -thickness / 2, (int) Math.ceil(Math.hypot(dx, dy)), (thickness + 1) / 2, color);
		pose.popMatrix();
	}

	private int completedQuests(Group entry) {
		return (int) entry.quests().stream().filter(candidate -> this.progress(candidate).done()).count();
	}

	private Progress progress(Quest entry) {
		return this.progress.getOrDefault(entry.advancement(), Progress.UNAVAILABLE);
	}

	private static Component status(Progress progress) {
		if (progress.total() == 0) {
			return message("unavailable");
		}
		if (progress.done()) {
			return message("complete");
		}
		return progress.total() == 1 ? message("incomplete") : message("steps", progress.completed(), progress.total());
	}

	private static Component message(String key, Object... args) {
		return Component.translatable("wildways.guide." + key, args);
	}

	private record Point(int x, int y) {
		Point shift(int dx, int dy) {
			return new Point(this.x + dx, this.y + dy);
		}

		Point offset(int radiusX, int radiusY, double angle) {
			return this.shift((int) Math.round(radiusX * Math.cos(angle)), (int) Math.round(radiusY * Math.sin(angle)));
		}
	}

	private record Link(Point from, Point to, boolean done) {
	}

	private enum Label { ABOVE, BELOW, LEFT, RIGHT, HIDDEN }

	private final class NodeButton extends AbstractButton {
		private final Point point;
		private final int radius;
		private final ItemStack icon;
		private final Label label;
		private final boolean done;
		private final Runnable open;

		NodeButton(Point point, int radius, ItemStack icon, Component title, Component status, Label label, boolean done, Runnable open) {
			super(point.x - radius, point.y - radius, radius * 2, radius * 2, title);
			this.point = point;
			this.radius = radius;
			this.icon = icon;
			this.label = label;
			this.done = done;
			this.open = open;
			this.setTooltip(Tooltip.create(label == Label.HIDDEN ? title.copy().append("\n").append(status) : status));
		}

		@Override
		public void onPress(InputWithModifiers input) {
			this.open.run();
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
			drawNode(graphics, this.point, this.radius, this.icon, this.done, this.isHoveredOrFocused());
			if (this.label != Label.HIDDEN) {
				QuestGuideScreen.this.drawLabel(graphics, this.point, this.radius, this.getMessage(), this.label,
					this.done ? COMPLETE : TEXT);
			}
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {
			this.defaultButtonNarrationText(output);
		}
	}
}
