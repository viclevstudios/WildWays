package com.viclev.wildways.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.viclev.wildways.ModItems;
import com.viclev.wildways.QuestGuide;
import com.viclev.wildways.QuestGuide.Group;
import com.viclev.wildways.QuestGuide.Progress;
import com.viclev.wildways.QuestGuide.Quest;
import com.viclev.wildways.client.QuestGuideIcons.Icon;
import com.viclev.wildways.client.QuestGuideLayout.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

final class QuestGuideScreen extends Screen {
	private static final Component TITLE = message("title");
	private static final int TEXT = 0xFFF8F7F2;
	private static final int SECONDARY = 0xFFDDDFDD;
	private static final int COMPLETE = 0xFFB6EF92;
	private static final int LINE = 0xFFF4F4F4;
	private static final int ROOT_ACCENT = 0xFFE5C582;
	private static final int HEADER = 36;
	private static final int FOOTER = 25;
	private static final int NODE_RADIUS = 14;
	private static final int ROOT_RADIUS = 21;
	private static final int LABEL_WIDTH = 96;
	private static final int GROUP_LABEL_WIDTH = 146;
	private static final int CENTER_GROUP_LABEL_WIDTH = 174;
	private static final int LABEL_LINE_HEIGHT = 10;
	private static final int PREVIEW_MARGIN = 8;
	private static final int PREVIEW_DESCRIPTION_WIDTH = 126;
	private static final int PREVIEW_MAX_WIDTH = 176;
	private static final int PREVIEW_HEADER_MIN_HEIGHT = 26;
	private static final Identifier PREVIEW_BODY = Identifier.withDefaultNamespace("advancements/title_box");
	private static final Identifier PREVIEW_HEADER = Identifier.withDefaultNamespace("advancements/box_unobtained");
	private static final Identifier PREVIEW_HEADER_COMPLETE = Identifier.withDefaultNamespace("advancements/box_obtained");
	private static final int RESET_WIDTH = 74;
	private static final double DRAG_THRESHOLD = 3;
	private static final double MIN_ZOOM = 0.25;
	private static final double MAX_ZOOM = 2.25;

	private final Map<Identifier, Progress> progress;
	private final Group group;
	private final Quest quest;
	private final QuestGuideScreen previous;
	private final List<Link> links = new ArrayList<>();
	private final List<NodeButton> nodes = new ArrayList<>();
	private double zoom;
	private double panX;
	private double panY;
	private double canvasX;
	private double canvasY;
	private int minX;
	private int maxX;
	private int minY;
	private int maxY;
	private boolean panning;
	private int panButton = -1;
	private double dragX;
	private double dragY;
	private NodeButton pressedNode;
	private double detailScroll;
	private int maxDetailScroll;

	private QuestGuideScreen(Map<Identifier, Progress> progress, Group group, Quest quest, QuestGuideScreen previous) {
		super(quest != null ? quest.title() : group != null ? group.title() : TITLE);
		this.progress = progress;
		this.group = group;
		this.quest = quest;
		this.previous = previous;
	}

	static void initialize() {
		QuestGuideIcons.initialize();
		ClientPlayNetworking.registerGlobalReceiver(QuestGuide.Open.TYPE, (open, context) -> {
			var client = context.client();
			if (client.gui.screen() == null && client.gui.overlay() == null) {
				client.gui.setScreen(new QuestGuideScreen(open.progress(), null, null, null));
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(QuestGuide.OpenQuest.TYPE, (open, context) -> {
			var gui = context.client().gui;
			if (gui.overlay() != null || (gui.screen() != null && !(gui.screen() instanceof ChatScreen))) {
				return;
			}
			for (Group group : QuestGuide.GROUPS) {
				for (Quest quest : group.quests()) {
					if (quest.advancement().equals(open.advancement())) {
						var overview = new QuestGuideScreen(open.progress(), null, null, null);
						var chapter = new QuestGuideScreen(open.progress(), group, null, overview);
						gui.setScreen(new QuestGuideScreen(open.progress(), group, quest, chapter));
						return;
					}
				}
			}
		});
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		if (this.previous != null) {
			this.minecraft.gui.setScreen(this.previous);
		} else {
			super.onClose();
		}
	}

	@Override
	protected void init() {
		this.panning = false;
		this.panButton = -1;
		this.pressedNode = null;
		this.links.clear();
		this.nodes.clear();
		if (this.previous != null) {
			this.addRenderableWidget(Button.builder(message("back"), button -> this.onClose()).bounds(10, 8, 64, 20).build());
		}
		this.addRenderableWidget(Button.builder(message("close"), button -> super.onClose())
			.bounds(this.width - 74, 8, 64, 20).build());
		if (this.quest != null) {
			return;
		}

		this.addRenderableWidget(Button.builder(message("reset_view"), button -> this.resetView())
			.bounds(this.width - RESET_WIDTH - 6, this.height - FOOTER + 3, RESET_WIDTH, 19).build());
		if (this.group == null) {
			this.layoutOverview();
		} else {
			this.layoutChapter();
		}
		this.minX = Math.min(0, this.nodes.stream().mapToInt(node -> node.point.x()).min().orElse(0));
		this.maxX = Math.max(0, this.nodes.stream().mapToInt(node -> node.point.x()).max().orElse(0));
		this.minY = Math.min(0, this.nodes.stream().mapToInt(node -> node.point.y()).min().orElse(0));
		this.maxY = Math.max(0, this.nodes.stream().mapToInt(node -> node.point.y()).max().orElse(0));
		if (this.zoom == 0) {
			this.resetView();
		} else {
			this.updateView();
		}
	}

	private void layoutOverview() {
		int count = QuestGuide.GROUPS.size();
		for (int index = 0; index < count; index++) {
			Group entry = QuestGuide.GROUPS.get(index);
			double angle = -Math.PI / 2 + index * Math.TAU / count;
			Point point = new Point((int)Math.round(156 * Math.cos(angle)), (int)Math.round(87 * Math.sin(angle)));
			int completed = this.completedQuests(entry);
			boolean done = completed == entry.quests().size();
			this.links.add(new Link(List.of(QuestGuideLayout.ROOT, point), done));
			var description = Component.translatableWithFallback("wildways.guide.group." + entry.id() + ".description", "");
			this.addNode(new NodeButton(point, QuestGuideIcons.icon("group." + entry.id(), entry.icon()), entry.title(),
				description, new Progress(completed, entry.quests().size()),
				() -> this.minecraft.gui.setScreen(new QuestGuideScreen(this.progress, entry, null, this))));
		}
	}

	private void layoutChapter() {
		var layout = QuestGuideLayout.chapter(this.group);
		for (var edge : layout.edges()) {
			Quest target = this.group.quests().stream().filter(entry -> entry.id().equals(edge.target())).findFirst().orElseThrow();
			this.links.add(new Link(edge.points(), this.progress(target).done()));
		}
		for (Quest entry : this.group.quests()) {
			Progress state = this.progress(entry);
			this.addNode(new NodeButton(Objects.requireNonNull(layout.positions().get(entry.id())),
				QuestGuideIcons.icon("quest." + entry.id(), entry.icon()), entry.title(),
				entry.description(), state,
				() -> this.minecraft.gui.setScreen(new QuestGuideScreen(this.progress, this.group, entry, this))));
		}
	}

	private void addNode(NodeButton button) {
		this.nodes.add(this.addWidget(button));
	}

	private void resetView() {
		double availableWidth = this.width - 20;
		double availableHeight = this.height - HEADER - FOOTER - 28;
		int horizontalPadding = (this.group == null ? GROUP_LABEL_WIDTH : LABEL_WIDTH) + 16;
		this.zoom = Math.max(MIN_ZOOM, Math.min(1, Math.min(availableWidth / (this.maxX - this.minX + horizontalPadding),
			availableHeight / (this.maxY - this.minY + 2 * NODE_RADIUS))));
		this.panX = -(this.minX + this.maxX) * this.zoom / 2;
		this.panY = -(this.minY + this.maxY) * this.zoom / 2 - 7;
		this.updateView();
	}

	private void updateView() {
		this.panX = Math.clamp(this.panX, 26 - this.width / 2.0 - this.maxX * this.zoom,
			this.width / 2.0 - 26 - this.minX * this.zoom);
		double halfHeight = Math.max(26, (this.height - HEADER - FOOTER) / 2.0);
		this.panY = Math.clamp(this.panY, 26 - halfHeight - this.maxY * this.zoom, halfHeight - 26 - this.minY * this.zoom);
		int guiScale = this.minecraft.getWindow().getGuiScale();
		this.canvasX = Math.rint((this.width / 2.0 + this.panX) * guiScale) / guiScale;
		this.canvasY = Math.rint((this.centerY() + this.panY) * guiScale) / guiScale;
		this.nodes.forEach(NodeButton::updatePosition);
	}

	private double centerY() {
		return (HEADER + this.height - FOOTER) / 2.0;
	}

	private boolean onCanvas(double x, double y) {
		return this.quest == null && x >= 0 && x < this.width && y >= HEADER && y < this.height - FOOTER;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		boolean left = event.button() == InputConstants.MOUSE_BUTTON_LEFT;
		if ((left || event.button() == InputConstants.MOUSE_BUTTON_RIGHT) && this.onCanvas(event.x(), event.y())) {
			this.panButton = event.button();
			this.panning = !left;
			this.dragX = 0;
			this.dragY = 0;
			this.pressedNode = left ? this.nodes.stream().filter(node -> node.isMouseOver(event.x(), event.y())).findFirst().orElse(null) : null;
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if (event.button() == this.panButton) {
			this.dragX += dx;
			this.dragY += dy;
			if (this.panning || Math.hypot(this.dragX, this.dragY) >= DRAG_THRESHOLD) {
				this.panning = true;
				this.panX += this.dragX;
				this.panY += this.dragY;
				this.dragX = 0;
				this.dragY = 0;
				this.updateView();
			}
			return true;
		}
		return super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (event.button() == this.panButton) {
			NodeButton clicked = this.panning ? null : this.pressedNode;
			this.panning = false;
			this.panButton = -1;
			this.pressedNode = null;
			if (clicked != null && clicked.isMouseOver(event.x(), event.y())) {
				this.setFocused(clicked);
				clicked.mouseClicked(event, false);
			}
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		if (this.onCanvas(x, y) && scrollY != 0) {
			double next = Math.clamp(this.zoom * Math.pow(1.18, scrollY), MIN_ZOOM, MAX_ZOOM);
			double ratio = next / this.zoom;
			double cursorX = x - this.width / 2.0;
			double cursorY = y - this.centerY();
			this.panX = cursorX + (this.panX - cursorX) * ratio;
			this.panY = cursorY + (this.panY - cursorY) * ratio;
			this.zoom = next;
			this.updateView();
			return true;
		}
		if (this.quest != null && this.maxDetailScroll > 0 && y >= HEADER && y < this.height - FOOTER && scrollY != 0) {
			this.detailScroll = Math.clamp(this.detailScroll - 18 * scrollY, 0, this.maxDetailScroll);
			return true;
		}
		return super.mouseScrolled(x, y, scrollX, scrollY);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		graphics.fill(0, 0, this.width, this.height, 0xCD3C3E40);
		graphics.fill(0, 0, this.width, HEADER, 0x90202122);
		graphics.fill(0, this.height - FOOTER, this.width, this.height, 0x90202122);
		graphics.fill(0, HEADER - 1, this.width, HEADER, 0x508F9497);
		graphics.fill(0, this.height - FOOTER, this.width, this.height - FOOTER + 1, 0x508F9497);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		Component heading = (this.group == null ? TITLE : this.group.title()).copy().withStyle(ChatFormatting.BOLD);
		var headingLines = this.wrapText(heading, Math.max(24, this.width - 156), 2);
		for (int row = 0; row < headingLines.size(); row++) {
			graphics.centeredText(this.font, headingLines.get(row), this.width / 2,
				(HEADER - headingLines.size() * LABEL_LINE_HEIGHT) / 2 + row * LABEL_LINE_HEIGHT, TEXT);
		}
		if (this.quest == null) {
			this.drawCanvas(graphics, mouseX, mouseY, delta);
		} else {
			this.drawQuest(graphics);
		}
		this.drawFooter(graphics);
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		if (this.quest == null && !this.panning) {
			this.drawHoveredPreview(graphics, mouseX, mouseY);
		}
	}

	private void drawCanvas(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		graphics.enableScissor(0, HEADER, this.width, this.height - FOOTER);
		var pose = graphics.pose();
		pose.pushMatrix();
		pose.translate((float)this.canvasX, (float)this.canvasY);
		pose.scale((float)this.zoom);
		for (Link link : this.links) {
			if (!link.done) {
				link.draw(graphics, LINE);
			}
		}
		for (Link link : this.links) {
			if (link.done) {
				link.draw(graphics, COMPLETE);
			}
		}
		Icon root = this.group == null ? QuestGuideIcons.icon("root", ModItems.QUEST_BOOK)
			: QuestGuideIcons.icon("group." + this.group.id(), this.group.icon());
		boolean done = this.group != null && this.completedQuests(this.group) == this.group.quests().size();
		octagon(graphics, QuestGuideLayout.ROOT, ROOT_RADIUS + 3, done ? COMPLETE : ROOT_ACCENT);
		octagon(graphics, QuestGuideLayout.ROOT, ROOT_RADIUS + 1, 0xFF25292B);
		drawNode(graphics, QuestGuideLayout.ROOT, ROOT_RADIUS, root, done, false);
		for (NodeButton node : this.nodes) {
			node.extractRenderState(graphics, this.panning ? -1 : mouseX, mouseY, delta);
		}
		pose.popMatrix();
		for (NodeButton node : this.nodes) {
			node.drawLabel(graphics);
		}
		graphics.disableScissor();
	}

	private void drawHoveredPreview(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		for (NodeButton node : this.nodes) {
			if (node.isMouseOver(mouseX, mouseY)) {
				this.drawPreview(graphics, node);
				return;
			}
		}
		if (this.minecraft.getLastInputType().isKeyboard() && this.getFocused() instanceof NodeButton node && node.visible) {
			this.drawPreview(graphics, node);
		}
	}

	private void drawPreview(GuiGraphicsExtractor graphics, NodeButton node) {
		int maxWidth = Math.min(PREVIEW_MAX_WIDTH, this.width - 2 * PREVIEW_MARGIN);
		if (maxWidth < 72) {
			return;
		}
		Component count = node.state.total() > 1
			? Component.literal(node.state.completed() + "/" + node.state.total()) : Component.empty();
		int countWidth = node.state.total() > 1 ? this.font.width(count) + 10 : 0;
		int titleWidth = this.font.width(node.getMessage()) + 38 + countWidth;
		int descriptionWidth = Math.min(PREVIEW_DESCRIPTION_WIDTH, this.font.width(node.description)) + 18;
		int width = Math.min(maxWidth, Math.max(88, Math.max(titleWidth, descriptionWidth)));
		var title = this.wrapText(node.getMessage(), Math.max(24, width - 38 - countWidth), 3);
		int titleHeight = title.size() * LABEL_LINE_HEIGHT;
		int headerHeight = PREVIEW_HEADER_MIN_HEIGHT + Math.max(0, titleHeight - LABEL_LINE_HEIGHT);
		int descriptionLimit = (this.height - 2 * PREVIEW_MARGIN - headerHeight - 12) / LABEL_LINE_HEIGHT;
		var description = node.description.getString().isBlank() || descriptionLimit < 1 ? List.<FormattedCharSequence>of()
			: this.wrapText(node.description, width - 18, descriptionLimit);
		// Leave room for the sprite's borders beyond the description text.
		int bodyHeight = description.isEmpty() ? 0 : Math.max(20, 12 + description.size() * LABEL_LINE_HEIGHT);
		int height = headerHeight + bodyHeight;
		if (height > this.height - 2 * PREVIEW_MARGIN) {
			return;
		}
		int right = node.getX() + node.getWidth() + 4;
		int left = node.getX() - width - 4;
		int x = right + width <= this.width - PREVIEW_MARGIN || left < PREVIEW_MARGIN && node.screenX < this.width / 2
			? right : left;
		x = Math.clamp(x, PREVIEW_MARGIN, this.width - width - PREVIEW_MARGIN);
		int y = Math.clamp(node.screenY - headerHeight / 2, PREVIEW_MARGIN, this.height - height - PREVIEW_MARGIN);

		graphics.nextStratum();
		if (bodyHeight > 0) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PREVIEW_BODY, x, y + headerHeight - 7, width, bodyHeight + 7);
		}
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, node.state.done() ? PREVIEW_HEADER_COMPLETE : PREVIEW_HEADER,
			x, y, width, headerHeight);
		if (!node.state.done() && node.state.completed() > 0 && node.state.total() > 0) {
			int filledWidth = width * node.state.completed() / node.state.total();
			graphics.enableScissor(x, y, x + filledWidth, y + headerHeight);
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PREVIEW_HEADER_COMPLETE, x, y, width, headerHeight);
			graphics.disableScissor();
		}
		node.icon.draw(graphics, x + 8, y + (headerHeight - 16) / 2);
		int titleY = y + (headerHeight - titleHeight) / 2;
		for (int row = 0; row < title.size(); row++) {
			graphics.text(this.font, title.get(row), x + 30, titleY + row * LABEL_LINE_HEIGHT, TEXT);
		}
		if (countWidth > 0) {
			graphics.text(this.font, count, x + width - countWidth + 2, y + (headerHeight - LABEL_LINE_HEIGHT) / 2, TEXT);
		}
		for (int row = 0; row < description.size(); row++) {
			graphics.text(this.font, description.get(row), x + 9, y + headerHeight + 5 + row * LABEL_LINE_HEIGHT, SECONDARY);
		}
	}

	private void drawFooter(GuiGraphicsExtractor graphics) {
		Component status;
		if (this.quest != null) {
			status = message("detail_hint");
		} else if (this.group != null) {
			status = message("overview_hint", this.completedQuests(this.group), this.group.quests().size());
		} else {
			int completed = QuestGuide.GROUPS.stream().mapToInt(this::completedQuests).sum();
			int total = QuestGuide.GROUPS.stream().mapToInt(entry -> entry.quests().size()).sum();
			status = message("overview_hint", completed, total);
		}
		int y = this.height - 16;
		int x = this.width / 2;
		if (this.quest == null) {
			x = Math.min(x, this.width - RESET_WIDTH - 14 - this.font.width(status) / 2);
		}
		graphics.centeredText(this.font, status, x, y, SECONDARY);
	}

	private void drawQuest(GuiGraphicsExtractor graphics) {
		int panelWidth = Math.min(344, this.width - 32);
		int left = (this.width - panelWidth) / 2;
		var description = this.font.split(this.quest.description(), panelWidth - 32);
		int available = Math.max(0, this.height - HEADER - FOOTER - 20);
		int panelHeight = Math.min(available, 86 + description.size() * 12);
		int top = (int)this.centerY() - panelHeight / 2;
		int bodyTop = top + 52;
		int bodyBottom = top + panelHeight - 28;
		this.maxDetailScroll = Math.max(0, description.size() * 12 - (bodyBottom - bodyTop));
		this.detailScroll = Math.min(this.detailScroll, this.maxDetailScroll);
		Progress state = this.progress(this.quest);
		graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xDC282A2C);
		graphics.fill(left, top, left + panelWidth, top + 1, 0xFF8F9497);
		drawNode(graphics, new Point(left + 30, top + 29), NODE_RADIUS,
			QuestGuideIcons.icon("quest." + this.quest.id(), this.quest.icon()), state.done(), false);
		var title = this.font.split(this.quest.title().copy().withStyle(ChatFormatting.BOLD), panelWidth - 66);
		for (int index = 0; index < Math.min(2, title.size()); index++) {
			graphics.text(this.font, title.get(index), left + 52, top + (title.size() == 1 ? 25 : 20) + index * 11, TEXT);
		}
		if (bodyBottom > bodyTop) {
			graphics.enableScissor(left + 12, bodyTop, left + panelWidth - 12, bodyBottom);
			int y = bodyTop - (int)this.detailScroll;
			for (var row : description) {
				if (y + 12 > bodyTop && y < bodyBottom) {
					graphics.text(this.font, row, left + 16, y, SECONDARY);
				}
				y += 12;
			}
			graphics.disableScissor();
		}
		graphics.fill(left + 16, bodyBottom + 3, left + panelWidth - 16, bodyBottom + 4, 0xFF595D60);
		graphics.text(this.font, status(state), left + 16, bodyBottom + 12, state.done() ? COMPLETE : TEXT);
	}

	private List<FormattedCharSequence> wrapText(Component text, int maxWidth, int maxLines) {
		var lines = this.font.splitIgnoringLanguage(text, maxWidth);
		if (lines.size() > maxLines) {
			lines = new ArrayList<>(lines.subList(0, maxLines));
			var lastLine = this.font.substrByWidth(lines.getLast(), Math.max(0, maxWidth - this.font.width("…")));
			lines.set(maxLines - 1, FormattedText.composite(lastLine, FormattedText.of("…")));
		}
		return Language.getInstance().getVisualOrder(lines);
	}

	private static void drawNode(GuiGraphicsExtractor graphics, Point point, int radius, Icon icon, boolean done, boolean highlighted) {
		int outline = done ? COMPLETE : highlighted ? LINE : 0xFFA6ACAF;
		octagon(graphics, point, radius, outline);
		octagon(graphics, point, radius - (highlighted ? 2 : 1), highlighted ? 0xFF474D50 : 0xFF303436);
		icon.draw(graphics, point.x() - 8, point.y() - 8);
		if (done) {
			Point badge = new Point(point.x() + radius - 3, point.y() - radius + 3);
			octagon(graphics, badge, 6, 0xFF263825);
			Point bend = new Point(badge.x() - 1, badge.y() + 2);
			line(graphics, new Point(badge.x() - 4, badge.y() - 1), bend, 2, COMPLETE);
			line(graphics, bend, new Point(badge.x() + 4, badge.y() - 3), 2, COMPLETE);
		}
	}

	private static void octagon(GuiGraphicsExtractor graphics, Point point, int radius, int color) {
		int corner = radius / 3;
		graphics.fill(point.x() - radius, point.y() - radius + corner, point.x() + radius, point.y() + radius - corner, color);
		// Overlap the corner rows so fractional zoom cannot leave seams through the fill.
		for (int inset = 1; inset <= corner; inset++) {
			int left = point.x() - radius + inset;
			int right = point.x() + radius - inset;
			int top = point.y() - radius + corner - inset;
			int bottom = point.y() + radius - corner + inset;
			graphics.fill(left, top, right, top + 2, color);
			graphics.fill(left, bottom - 2, right, bottom, color);
		}
	}

	private static void line(GuiGraphicsExtractor graphics, Point from, Point to, int thickness, int color) {
		int dx = to.x() - from.x();
		int dy = to.y() - from.y();
		var pose = graphics.pose();
		pose.pushMatrix();
		if (dx == 0 || dy == 0) {
			// Center both strokes on the same corner, with overlapping ends.
			pose.scale(0.5F);
			graphics.fill(2 * Math.min(from.x(), to.x()) - thickness, 2 * Math.min(from.y(), to.y()) - thickness,
				2 * Math.max(from.x(), to.x()) + thickness, 2 * Math.max(from.y(), to.y()) + thickness, color);
		} else {
			pose.translate(from.x(), from.y());
			pose.rotate((float)Math.atan2(dy, dx));
			pose.scale(0.5F);
			graphics.fill(0, -thickness, (int)Math.ceil(2 * Math.hypot(dx, dy)), thickness, color);
		}
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

	private record Link(List<Point> points, boolean done) {
		void draw(GuiGraphicsExtractor graphics, int color) {
			for (int i = 1; i < this.points.size(); i++) {
				Point from = this.points.get(i - 1);
				Point to = this.points.get(i);
				if (!from.equals(to)) {
					line(graphics, from, to, 1, color);
				}
			}
		}
	}

	private final class NodeButton extends AbstractButton {
		private final Point point;
		private final Icon icon;
		private final Component description;
		private final Progress state;
		private final Runnable open;
		private int screenX;
		private int screenY;
		private int labelX;
		private int labelY;
		private float labelScale;
		private List<FormattedCharSequence> labelLines = List.of();

		NodeButton(Point point, Icon icon, Component title, Component description, Progress state, Runnable open) {
			super(0, 0, 0, 0, title);
			this.point = point;
			this.icon = icon;
			this.description = description;
			this.state = state;
			this.open = open;
		}

		void updatePosition() {
			this.screenX = (int)Math.round(canvasX + this.point.x() * zoom);
			this.screenY = (int)Math.round(canvasY + this.point.y() * zoom);
			int radius = Math.max(4, (int)Math.round(NODE_RADIUS * zoom));
			this.setRectangle(2 * radius, 2 * radius, this.screenX - radius, this.screenY - radius);
			this.visible = this.screenX + radius > 0 && this.screenX - radius < QuestGuideScreen.this.width
				&& this.screenY + radius > HEADER && this.screenY - radius < QuestGuideScreen.this.height - FOOTER;
			if (!this.visible && QuestGuideScreen.this.getFocused() == this) {
				QuestGuideScreen.this.setFocused(null);
			}
			if (this.visible) {
				this.updateLabel(radius);
			}
		}

		private void updateLabel(int radius) {
			int screenWidth = QuestGuideScreen.this.width;
			int preferredWidth = group != null ? LABEL_WIDTH : this.point.x() == 0 ? CENTER_GROUP_LABEL_WIDTH : GROUP_LABEL_WIDTH;
			int maxWidth = Math.max(12, Math.min(screenWidth - 12, (int)(preferredWidth * Math.min(1.25, zoom))));
			this.labelScale = Math.clamp((float)zoom * 0.82F, 0.55F, 0.86F);
			int gap = zoom < 0.5 ? 1 : 3;
			double spaceBelowNode = (QuestGuideLayout.ROW - 2 * NODE_RADIUS) * zoom - gap;
			int maxLines = Math.clamp((int)(spaceBelowNode / (LABEL_LINE_HEIGHT * this.labelScale)), 1, 2);
			this.labelLines = wrapText(this.getMessage(), (int)(maxWidth / this.labelScale), maxLines);
			int halfWidth = Math.min(screenWidth / 2, maxWidth / 2 + 6);
			this.labelX = Math.clamp(this.screenX, halfWidth, screenWidth - halfWidth);
			this.labelY = this.screenY + radius + gap;
			int labelHeight = (int)Math.ceil(this.labelLines.size() * LABEL_LINE_HEIGHT * this.labelScale);
			if (this.labelY + labelHeight > QuestGuideScreen.this.height - FOOTER) {
				this.labelY = this.screenY - radius - gap - labelHeight;
			}
		}

		void drawLabel(GuiGraphicsExtractor graphics) {
			if (!this.visible) {
				return;
			}
			var pose = graphics.pose();
			pose.pushMatrix();
			pose.translate(this.labelX, this.labelY);
			pose.scale(this.labelScale);
			for (int row = 0; row < this.labelLines.size(); row++) {
				graphics.centeredText(QuestGuideScreen.this.font, this.labelLines.get(row), 0, row * LABEL_LINE_HEIGHT,
					this.state.done() ? COMPLETE : TEXT);
			}
			pose.popMatrix();
		}

		@Override
		public boolean isMouseOver(double x, double y) {
			return !panning && onCanvas(x, y) && super.isMouseOver(x, y);
		}

		@Override
		public void onPress(InputWithModifiers input) {
			this.open.run();
		}

		@Override
		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
			drawNode(graphics, this.point, NODE_RADIUS, this.icon, this.state.done(), this.isHoveredOrFocused());
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {
			this.defaultButtonNarrationText(output);
			if (!this.description.getString().isBlank()) {
				output.add(NarratedElementType.HINT, this.description);
			}
		}
	}
}
