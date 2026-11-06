package com.viclev.wildways;

import java.util.Random;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class FishingMinigame {
	public static final AttachmentType<FishingBait.Type> CAST_BAIT = AttachmentRegistry.create(
		Wildways.id("fishing_bobber_bait"), builder -> builder.syncWith(ByteBufCodecs.fromCodec(FishingBait.Type.CODEC), AttachmentSyncPredicate.all())
	);
	public static final int READY_MILLIS = 400;
	private static final int SAMPLE_MILLIS = 50;
	private static final float TARGET_SPEED = 1.75F;
	private static final float WIDTH_SCALE = 0.75F;
	private static final Rules STANDARD = new Rules(5_000, 0.09F * WIDTH_SCALE, 0.16F * WIDTH_SCALE);
	private static final Rules CANDIED = new Rules(3_000, STANDARD.centerHalfWidth * 1.5F, STANDARD.edgeHalfWidth * 1.5F);

	private FishingMinigame() {
	}

	public static void initialize() {
		PayloadTypeRegistry.clientboundPlay().register(Start.TYPE, Start.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(Finish.TYPE, Finish.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(Finish.TYPE, (packet, context) -> {
			ServerPlayer player = context.player();
			if (player.fishing != null && player.fishing.getId() == packet.hookId()) {
				((Hook) player.fishing).wildways$finishRound(player, packet.positions());
			}
		});
		UseItemCallback.EVENT.register((player, level, hand) -> {
			if (player instanceof ServerPlayer serverPlayer && !player.isSpectator()
				&& player.fishing != null && player.getItemInHand(hand).is(Items.FISHING_ROD)
				&& ServerPlayNetworking.canSend(serverPlayer, Start.TYPE)
				&& ((Hook) player.fishing).wildways$startRound(serverPlayer, hand)) {
				return InteractionResult.SUCCESS_SERVER;
			}
			return InteractionResult.PASS;
		});
	}

	public record Rules(int duration, float centerHalfWidth, float edgeHalfWidth) {
		public static Rules forRound(boolean candied) {
			return candied ? CANDIED : STANDARD;
		}

		public int sampleCount() {
			return (this.duration - READY_MILLIS) / SAMPLE_MILLIS;
		}

		public static long sampleTime(int index) {
			return READY_MILLIS + (long) index * SAMPLE_MILLIS + SAMPLE_MILLIS / 2;
		}

		public int points(byte position, float target) {
			float distance = Math.abs(Byte.toUnsignedInt(position) / 255.0F - target);
			if (distance <= this.centerHalfWidth) {
				return 2;
			}
			return distance <= this.edgeHalfWidth ? 1 : 0;
		}

		public float catchChance(int points) {
			return points / (2.0F * this.sampleCount());
		}
	}

	public interface Hook {
		boolean wildways$startRound(ServerPlayer player, InteractionHand hand);

		void wildways$finishRound(ServerPlayer player, byte[] positions);
	}

	public record Round(InteractionHand hand, ItemStack rod, long startedAt, long seed, Rules rules) {
	}

	public record Path(float firstTurn, float secondTurn, float end) {
		public static Path fromSeed(long seed) {
			Random random = new Random(seed);
			int direction = random.nextBoolean() ? 1 : -1;
			// Keep the travel distance independent of the widths of the scoring zones.
			float scale = 0.25F / 0.28F;
			return new Path(
				0.5F + direction * scale * (0.15F + 0.12F * random.nextFloat()),
				0.5F - direction * scale * (0.16F + 0.12F * random.nextFloat()),
				0.5F + direction * scale * (0.20F + 0.08F * random.nextFloat())
			);
		}

		public float targetAt(long elapsed) {
			float progress = Mth.clamp((float) (elapsed - READY_MILLIS) / (STANDARD.duration - READY_MILLIS), 0.0F, 1.0F);
			float phase = (progress * TARGET_SPEED) % 2.0F;
			float leg = Math.min(phase, 2.0F - phase) * 3.0F;
			if (leg < 1.0F) {
				return moveBetween(0.5F, firstTurn, leg);
			}
			if (leg < 2.0F) {
				return moveBetween(firstTurn, secondTurn, leg - 1.0F);
			}
			return moveBetween(secondTurn, end, leg - 2.0F);
		}

		private static float moveBetween(float from, float to, float progress) {
			return Mth.lerp(progress * progress * (3.0F - 2.0F * progress), from, to);
		}
	}

	public static byte pointerSample(float position) {
		return (byte) Math.round(position * 255);
	}

	public record Start(int hookId, long seed, boolean candied) implements CustomPacketPayload {
		public static final Type<Start> TYPE = new Type<>(Wildways.id("fishing_start"));
		private static final StreamCodec<RegistryFriendlyByteBuf, Start> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, Start::hookId,
			ByteBufCodecs.LONG, Start::seed,
			ByteBufCodecs.BOOL, Start::candied,
			Start::new
		);

		@Override
		public Type<Start> type() {
			return TYPE;
		}
	}

	public record Finish(int hookId, byte[] positions) implements CustomPacketPayload {
		public static final byte[] CANCELLED = new byte[0];
		public static final Type<Finish> TYPE = new Type<>(Wildways.id("fishing_finish"));
		private static final StreamCodec<RegistryFriendlyByteBuf, Finish> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, Finish::hookId,
			ByteBufCodecs.byteArray(STANDARD.sampleCount()), Finish::positions,
			Finish::new
		);

		@Override
		public Type<Finish> type() {
			return TYPE;
		}
	}
}
