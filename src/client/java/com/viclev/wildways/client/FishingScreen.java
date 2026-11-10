package com.viclev.wildways.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.viclev.wildways.FishingMinigame;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

final class FishingScreen extends Screen {
	private static final Component INSTRUCTION = Component.translatable("wildways.fishing.follow");
	private static final Component GET_READY = Component.translatable("wildways.fishing.ready");
	private static final int TRACK_COLOR = 0xFF273D48;
	private static final int CENTER_COLOR = 0xFFAAE8C7;
	private static final int EDGE_COLOR = 0xFFF1C477;
	private static final int MISS_COLOR = 0xFFE8B9B4;

	private final int hookId;
	private final FishingMinigame.Path path;
	private final FishingMinigame.Rules rules;
	private final long openedAt;
	private final byte[] positions;
	private byte lastPosition = FishingMinigame.pointerSample(0.5F);
	private int samples;
	private int points;
	private boolean submitted;

	private FishingScreen(FishingMinigame.Start start) {
		super(INSTRUCTION);
		this.hookId = start.hookId();
		this.rules = FishingMinigame.Rules.forRound(start.candied());
		this.path = FishingMinigame.Path.fromSeed(start.seed());
		this.positions = new byte[this.rules.sampleCount()];
		this.openedAt = Util.getMillis();
	}

	static void initialize() {
		ClientPlayNetworking.registerGlobalReceiver(FishingMinigame.Start.TYPE, (start, context) -> {
			var client = context.client();
			if (client.gui.screen() == null && client.gui.overlay() == null && client.isWindowActive()) {
				client.gui.setScreen(new FishingScreen(start));
			} else {
				sendSamples(start.hookId(), FishingMinigame.Finish.CANCELLED);
			}
		});
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.key() == InputConstants.KEY_LSHIFT || event.key() == InputConstants.KEY_RSHIFT) {
			this.onClose();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		// Keep the bobber and surrounding water visible behind the track.
	}

	@Override
	public void tick() {
		var player = this.minecraft.player;
		if (!this.minecraft.isWindowActive() || player == null || player.fishing == null || player.fishing.getId() != this.hookId) {
			this.onClose();
			return;
		}
		long elapsed = Util.getMillis() - this.openedAt;
		this.recordSamples(elapsed, FishingMinigame.pointerSample(this.pointerPosition()));
		if (elapsed >= this.rules.duration()) {
			this.submitted = true;
			sendSamples(this.hookId, this.positions);
			this.onClose();
		}
	}

	@Override
	public void removed() {
		if (!this.submitted) {
			sendSamples(this.hookId, FishingMinigame.Finish.CANCELLED);
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		int trackWidth = this.trackWidth();
		int left = (this.width - trackWidth) / 2;
		int right = left + trackWidth;
		int top = this.height / 2 + Math.min(28, this.height / 8);
		long elapsed = Util.getMillis() - this.openedAt;
		float target = this.path.targetAt(elapsed);
		float position = this.pointerPosition();
		byte pointer = FishingMinigame.pointerSample(position);
		this.recordSamples(elapsed, pointer);
		int marker = left + Math.round(trackWidth * position);
		int zonePoints = this.rules.points(pointer, target);
		int chance = this.samples == 0 ? zonePoints * 50 : Math.round(this.points * 50.0F / this.samples);
		float timeLeft = Mth.clamp((float) (this.rules.duration() - elapsed)
			/ (this.rules.duration() - FishingMinigame.READY_MILLIS), 0.0F, 1.0F);

		graphics.fill(left - 7, top - 18, right + 7, top + 35, 0xFF101922);
		graphics.fill(left - 6, top - 17, right + 6, top + 34, 0xFF1D2B35);
		graphics.fill(left - 6, top - 17, right + 6, top - 16, 0xFF52636C);
		graphics.centeredText(this.font, elapsed < FishingMinigame.READY_MILLIS ? GET_READY : INSTRUCTION,
			this.width / 2, top - 13, 0xFFF4F3E9);
		graphics.fill(left - 2, top - 2, right + 2, top + 12, 0xFF101922);
		graphics.fill(left, top, right, top + 10, TRACK_COLOR);
		graphics.fill(left, top + 9, right, top + 10, 0xFF405966);
		int edgeLeft = left + Math.round(trackWidth * (target - this.rules.edgeHalfWidth()));
		int edgeRight = left + Math.round(trackWidth * (target + this.rules.edgeHalfWidth()));
		graphics.fill(edgeLeft, top + 1, edgeRight, top + 9, 0xFFAC783B);
		graphics.fill(edgeLeft + 1, top + 2, edgeRight - 1, top + 7, EDGE_COLOR);
		int centerLeft = left + Math.round(trackWidth * (target - this.rules.centerHalfWidth()));
		int centerRight = left + Math.round(trackWidth * (target + this.rules.centerHalfWidth()));
		graphics.fill(centerLeft, top, centerRight, top + 10, 0xFF356B60);
		graphics.fill(centerLeft + 1, top + 1, centerRight - 1, top + 8, CENTER_COLOR);
		graphics.fill(centerLeft + 1, top + 1, centerRight - 1, top + 2, 0xFFDAF5E5);
		graphics.fill(marker - 2, top - 3, marker + 3, top + 13, 0xFF101922);
		graphics.fill(marker - 2, top - 3, marker + 3, top - 2, 0xFFFFF7E0);
		graphics.fill(marker, top - 2, marker + 1, top + 12, 0xFFFFF7E0);
		graphics.fill(marker - 2, top + 12, marker + 3, top + 13, 0xFFFFF7E0);
		graphics.fill(left, top + 16, right, top + 18, 0xFF101922);
		graphics.fill(left, top + 16, left + Math.round(trackWidth * timeLeft), top + 18, 0xFF7EACBC);

		int chanceColor = switch (zonePoints) {
			case 2 -> CENTER_COLOR;
			case 1 -> EDGE_COLOR;
			default -> MISS_COLOR;
		};
		graphics.centeredText(this.font, Component.translatable("wildways.fishing.chance", chance),
			this.width / 2, top + 23, chanceColor);
	}

	private void recordSamples(long elapsed, byte pointer) {
		while (this.samples < this.positions.length && FishingMinigame.Rules.sampleTime(this.samples) <= elapsed) {
			// Unobserved intervals use the last known position, so late input cannot score retroactively.
			this.positions[this.samples] = this.lastPosition;
			this.points += this.rules.points(this.lastPosition, this.path.targetAt(FishingMinigame.Rules.sampleTime(this.samples)));
			this.samples++;
		}
		this.lastPosition = pointer;
	}

	private int trackWidth() {
		return Math.max(1, Math.min(208, this.width - 32));
	}

	private float pointerPosition() {
		int trackWidth = this.trackWidth();
		int left = (this.width - trackWidth) / 2;
		double x = this.minecraft.mouseHandler.getScaledXPos(this.minecraft.getWindow());
		return (float) Mth.clamp((x - left) / trackWidth, 0.0, 1.0);
	}

	private static void sendSamples(int hookId, byte[] positions) {
		if (ClientPlayNetworking.canSend(FishingMinigame.Finish.TYPE)) {
			ClientPlayNetworking.send(new FishingMinigame.Finish(hookId, positions));
		}
	}
}
