package com.viclev.wildways.client;

import com.viclev.wildways.QuestGuide.Group;
import com.viclev.wildways.QuestGuide.Quest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class QuestGuideLayout {
	static final Point ROOT = new Point(0, 0);
	private static final int COLUMN = 116;
	private static final int ROW = 56;

	private QuestGuideLayout() {
	}

	static Chapter chapter(Group group) {
		return switch (group.id()) {
			case "end" -> end(group.quests());
			case "wither" -> wither(group.quests());
			default -> tree(group.quests());
		};
	}

	private static Chapter tree(List<Quest> quests) {
		Map<String, List<Quest>> children = new HashMap<>();
		for (Quest quest : quests) {
			children.computeIfAbsent(quest.parent(), ignored -> new ArrayList<>()).add(quest);
		}
		List<Quest> left = new ArrayList<>();
		List<Quest> right = new ArrayList<>();
		int leftRows = 0;
		int rightRows = 0;
		for (Quest quest : children.getOrDefault(null, List.of())) {
			int rows = rows(quest, children);
			if (rightRows <= leftRows) {
				right.add(quest);
				rightRows += rows;
			} else {
				left.add(quest);
				leftRows += rows;
			}
		}
		Map<String, Point> positions = new HashMap<>();
		positionBranches(right, 1, -rightRows * ROW / 2, children, positions);
		positionBranches(left, -1, -leftRows * ROW / 2, children, positions);
		return new Chapter(positions, quests.stream().map(quest -> {
			Point parent = quest.parent() == null ? ROOT : positions.get(quest.parent());
			return branch(parent, positions.get(quest.id()), quest.id());
		}).toList());
	}

	private static int rows(Quest quest, Map<String, List<Quest>> children) {
		return Math.max(1, children.getOrDefault(quest.id(), List.of()).stream().mapToInt(child -> rows(child, children)).sum());
	}

	private static void positionBranches(List<Quest> branches, int column, int top,
		Map<String, List<Quest>> children, Map<String, Point> positions) {
		for (Quest quest : branches) {
			int height = rows(quest, children) * ROW;
			positions.put(quest.id(), new Point(column * COLUMN, top + height / 2));
			positionBranches(children.getOrDefault(quest.id(), List.of()), column + Integer.signum(column), top, children, positions);
			top += height;
		}
	}

	private static Chapter end(List<Quest> quests) {
		Map<String, Point> positions = new HashMap<>();
		List<Edge> links = new ArrayList<>();
		List<Quest> eyes = quests.stream().filter(quest -> !quest.id().equals("collect_portal_eyes") && quest.parent() == null).toList();
		int upperCount = (eyes.size() + 1) / 2;
		for (int i = 0; i < eyes.size(); i++) {
			boolean upper = i < upperCount;
			int count = upper ? upperCount : eyes.size() - upperCount;
			int column = upper ? i : i - upperCount;
			int y = upper ? -104 : 104;
			Point point = new Point((2 * column - count + 1) * 50, y);
			String id = eyes.get(i).id();
			positions.put(id, point);
			links.add(new Edge(id, List.of(ROOT, new Point(0, y / 2), new Point(point.x, y / 2), point)));
		}
		Point collection = new Point(COLUMN + 24, 0);
		Point portal = new Point(2 * COLUMN + 24, 0);
		positions.put("collect_portal_eyes", collection);
		positions.put("activate_end_portal", portal);
		links.add(branch(ROOT, collection, "collect_portal_eyes"));
		links.add(branch(collection, portal, "activate_end_portal"));
		return new Chapter(positions, links);
	}

	private static Chapter wither(List<Quest> quests) {
		var positions = Map.of(
			"find_wither_skull", new Point(COLUMN, 0),
			"find_sculk_wither_skull", new Point(COLUMN, -ROW),
			"find_gold_wither_skull", new Point(COLUMN, ROW),
			"summon_wither", new Point(2 * COLUMN, 0),
			"defeat_wither", new Point(3 * COLUMN, 0)
		);
		List<Edge> edges = new ArrayList<>();
		for (Quest quest : quests) {
			Point point = positions.get(quest.id());
			if (quest.parent() == null) {
				edges.add(branch(ROOT, point, quest.id()));
				edges.add(branch(point, positions.get("summon_wither"), "summon_wither"));
			} else if (quest.id().equals("defeat_wither")) {
				edges.add(branch(positions.get(quest.parent()), point, quest.id()));
			}
		}
		return new Chapter(positions, edges);
	}

	private static Edge branch(Point from, Point to, String id) {
		int split = (from.x + to.x) / 2;
		return new Edge(id, List.of(from, new Point(split, from.y), new Point(split, to.y), to));
	}

	record Point(int x, int y) {}
	record Edge(String target, List<Point> points) {}
	record Chapter(Map<String, Point> positions, List<Edge> edges) {}
}
