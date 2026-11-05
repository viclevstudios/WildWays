package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.viclev.wildways.FishingBait;
import com.viclev.wildways.FishingMinigame;
import com.viclev.wildways.FishingMinigame.Round;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public abstract class FishingMinigameMixin extends Projectile implements FishingMinigame.Hook {
	@Unique
	private static final int WILDWAYS_REPLY_GRACE_MILLIS = 4_000;
	@Shadow
	private int nibble;
	@Shadow @Final
	private int lureSpeed;
	@Unique
	private Round wildways$round;
	@Unique
	private ItemStack wildways$castRod;
	@Unique
	private FishingBait.Type wildways$biteBait;

	protected FishingMinigameMixin(EntityType<? extends Projectile> type, Level level) {
		super(type, level);
	}

	@Override
	public void applyOnProjectileSpawned(ServerLevel level, ItemStack rod) {
		super.applyOnProjectileSpawned(level, rod);
		this.wildways$castRod = rod;
	}

	@ModifyExpressionValue(method = "catchingFish", at = @At(value = "INVOKE", ordinal = 0,
		target = "Lnet/minecraft/util/Mth;nextInt(Lnet/minecraft/util/RandomSource;II)I"))
	private int wildways$extendBiteWindow(int ticks) {
		return Math.round(ticks * 1.5F);
	}

	@ModifyExpressionValue(method = "catchingFish", at = @At(value = "INVOKE", ordinal = 1,
		target = "Lnet/minecraft/util/Mth;nextInt(Lnet/minecraft/util/RandomSource;II)I"))
	private int wildways$shortenFishApproach(int ticks) {
		return Math.ceilDiv(ticks, 2);
	}

	@ModifyExpressionValue(method = "catchingFish", at = @At(value = "INVOKE", ordinal = 2,
		target = "Lnet/minecraft/util/Mth;nextInt(Lnet/minecraft/util/RandomSource;II)I"))
	private int wildways$shortenFishingWait(int ticks) {
		// Vanilla subtracts lureSpeed immediately after this roll.
		return this.lureSpeed + Math.ceilDiv(ticks - this.lureSpeed, 2);
	}

	@WrapOperation(method = "catchingFish", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/syncher/SynchedEntityData;set(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;)V"))
	private void wildways$useBaitOnBite(SynchedEntityData data, EntityDataAccessor<?> accessor, Object value, Operation<Void> original) {
		original.call(data, accessor, value);
		this.wildways$biteBait = null;
		ItemStack rod = this.wildways$castRod;
		FishingHook hook = (FishingHook) (Object) this;
		if (Boolean.TRUE.equals(value) && rod != null && hook.getPlayerOwner() instanceof ServerPlayer player
			&& (player.getMainHandItem() == rod || player.getOffhandItem() == rod)) {
			this.wildways$biteBait = FishingBait.consume(rod);
		}
	}

	@WrapOperation(method = "retrieve", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"))
	private ObjectArrayList<ItemStack> wildways$rollBaitLoot(LootTable table, LootParams params,
		Operation<ObjectArrayList<ItemStack>> original) {
		FishingHook hook = (FishingHook) (Object) this;
		return this.wildways$biteBait == null ? original.call(table, params)
			: FishingBait.catchLoot(this.wildways$biteBait, table, params, hook.isOpenWaterFishing(), this.random);
	}

	@Override
	public boolean wildways$startRound(ServerPlayer player, InteractionHand hand) {
		if (this.wildways$round != null) {
			return true;
		}
		FishingHook hook = (FishingHook) (Object) this;
		if (this.nibble <= 0 || hook.getHookedIn() != null || !wildways$isValid(hook, player)) {
			return false;
		}
		long seed = hook.level().getRandom().nextLong();
		boolean candied = this.wildways$biteBait == FishingBait.Type.CANDIED_BAIT;
		this.wildways$round = new Round(hand, player.getItemInHand(hand), Util.getMillis(), seed, FishingMinigame.Rules.forRound(candied));
		ServerPlayNetworking.send(player, new FishingMinigame.Start(hook.getId(), seed, candied));
		return true;
	}

	@Override
	public void wildways$finishRound(ServerPlayer player, byte[] positions) {
		Round round = this.wildways$round;
		FishingHook hook = (FishingHook) (Object) this;
		if (round == null || hook.getPlayerOwner() != player) {
			return;
		}
		long elapsed = Util.getMillis() - round.startedAt();
		if (positions.length != round.rules().sampleCount() || !wildways$canReel(hook, player, round) || wildways$expired(round, elapsed)) {
			hook.discard();
			return;
		}
		if (elapsed < round.rules().duration()) {
			return;
		}

		this.wildways$round = null;
		FishingMinigame.Path path = FishingMinigame.Path.fromSeed(round.seed());
		int points = 0;
		for (int index = 0; index < positions.length; index++) {
			points += round.rules().points(positions[index], path.targetAt(FishingMinigame.Rules.sampleTime(index)));
		}
		boolean caught = hook.level().getRandom().nextFloat() < round.rules().catchChance(points);
		if (!caught) {
			this.nibble = 0;
		}
		round.rod().use(player.level(), player, round.hand());
		if (!caught) {
			player.sendOverlayMessage(Component.translatable("wildways.fishing.escaped"));
		}
	}

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void wildways$expireFishingRound(CallbackInfo ci) {
		Round round = this.wildways$round;
		if (round != null) {
			FishingHook hook = (FishingHook) (Object) this;
			if (!(hook.getPlayerOwner() instanceof ServerPlayer player) || !wildways$canReel(hook, player, round)
				|| wildways$expired(round, Util.getMillis() - round.startedAt())) {
				hook.discard();
				ci.cancel();
			}
		}
	}

	@Inject(method = "catchingFish", at = @At("HEAD"), cancellable = true)
	private void wildways$holdBite(CallbackInfo ci) {
		if (this.wildways$round != null) {
			ci.cancel();
		}
	}

	@Unique
	private static boolean wildways$expired(Round round, long elapsed) {
		return elapsed > round.rules().duration() + WILDWAYS_REPLY_GRACE_MILLIS;
	}

	@Unique
	private static boolean wildways$isValid(FishingHook hook, ServerPlayer player) {
		return !hook.isRemoved() && player.fishing == hook && hook.getPlayerOwner() == player
			&& hook.level() == player.level() && player.canInteractWithLevel() && !player.isSpectator()
			&& hook.distanceToSqr(player) <= 1024.0;
	}

	@Unique
	private static boolean wildways$canReel(FishingHook hook, ServerPlayer player, Round round) {
		return wildways$isValid(hook, player) && hook.getHookedIn() == null
			&& player.getItemInHand(round.hand()) == round.rod() && round.rod().is(Items.FISHING_ROD);
	}
}
