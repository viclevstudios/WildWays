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
			this.layoutBranches(null, this.center, 1, this.branchDepth(null), 0, Math.TAU);
		}
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
			this.addRenderableWidget(new NodeButton(point, entry.icon().getDefaultInstance(), entry.title(),
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
			Progress state = this.progress(entry);
			Label label = point.y < this.center.y || (point.y == this.center.y && depth % 2 == 1)
				? Label.ABOVE : Label.BELOW;
			this.links.add(new Link(from, point, state.done()));
			this.addRenderableWidget(new NodeButton(point, entry.icon().getDefaultInstance(), entry.title(),
				status(state), label, state.done(),
				() -> this.minecraft.gui.setScreen(new QuestGuideScreen(this.progress, this.group, entry))));
			this.layoutBranches(entry.id(), point, depth + 1, maxDepth, angle, Math.min(spacing, Math.PI * 0.7));
		}
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
			if (this.group != null) {
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
		Point offset(int radiusX, int radiusY, double angle) {
			return new Point(this.x + (int) Math.round(radiusX * Math.cos(angle)),
				this.y + (int) Math.round(radiusY * Math.sin(angle)));
		}
	}

	private record Link(Point from, Point to, boolean done) {
	}

	private enum Label { ABOVE, BELOW, LEFT, RIGHT }

	private final class NodeButton extends AbstractButton {
		private final Point point;
		private final ItemStack icon;
		private final Label label;
		private final boolean done;
		private final Runnable open;

		NodeButton(Point point, ItemStack icon, Component title, Component status, Label label, boolean done, Runnable open) {
			super(point.x - NODE_RADIUS, point.y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2, title);
			this.point = point;
			this.icon = icon;
			this.label = label;
			this.done = done;
			this.open = open;
			this.setTooltip(Tooltip.create(status));
		}

		@Override
		public void onPress(InputWithModifiers input) {
			this.open.run();
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
			drawNode(graphics, this.point, NODE_RADIUS, this.icon, this.done, this.isHoveredOrFocused());
			QuestGuideScreen.this.drawLabel(graphics, this.point, NODE_RADIUS, this.getMessage(), this.label,
				this.done ? COMPLETE : TEXT);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {
			this.defaultButtonNarrationText(output);
		}
	}
}
