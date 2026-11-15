package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.viclev.wildways.WitherSpawnExplosion;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherBoss.class)
public abstract class WitherCombatMixin extends Monster {
	@Unique private static final int WILDWAYS_WINDUP_TICKS = 24;
	@Unique private static final int WILDWAYS_RECOVERY_TICKS = 36;
	@Unique private static final double WILDWAYS_DASH_SPEED = 1.45;
	@Unique private static final double WILDWAYS_IMPACT_RADIUS = 2.0;
	@Unique private Vec3 wildways$landingPosition = Vec3.ZERO;
	@Unique private int wildways$dashCooldown = 80;
	@Unique private int wildways$windupTicks;
	@Unique private int wildways$recoveryTicks;
	@Unique private boolean wildways$diving;

	protected WitherCombatMixin(EntityType<? extends Monster> type, Level level) {
		super(type, level);
	}

	@WrapOperation(
		method = "customServerAiStep",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)V")
	)
	private void wildways$explodeOnSpawn(
		ServerLevel level, Entity source, double x, double y, double z, float radius, boolean fire,
		Level.ExplosionInteraction interaction, Operation<Void> original
	) {
		WitherBoss wither = (WitherBoss) source;
		level.explode(wither, Explosion.getDefaultDamageSource(level, wither), new WitherSpawnExplosion(wither),
			x, y, z, radius, fire, interaction);
	}

	@Inject(method = "customServerAiStep", at = @At("HEAD"))
	private void wildways$prepareDash(ServerLevel level, CallbackInfo ci) {
		WitherBoss wither = (WitherBoss) (Object) this;
		if (wither.getInvulnerableTicks() > 0 || wither.isPowered()) {
			this.wildways$windupTicks = 0;
			this.wildways$diving = false;
			return;
		}
		if (this.wildways$isDashActive() || --this.wildways$dashCooldown > 0) {
			return;
		}
		this.wildways$dashCooldown = 10;
		if (!(this.getTarget() instanceof Player target) || !this.canAttack(target) || !this.hasLineOfSight(target)) {
			return;
		}

		Vec3 feet = target.position();
		Vec3 aboveFeet = feet.add(0.0, 0.25, 0.0);
		var ground = level.clip(new ClipContext(aboveFeet, feet.add(0.0, -2.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
		Vec3 landing = ground.getLocation();
		Vec3 offset = landing.subtract(this.position());
		if (ground.getType() != HitResult.Type.BLOCK || offset.y > -3.0 || offset.y < -12.0 || offset.horizontalDistanceSqr() > 100.0
			|| level.clip(new ClipContext(this.position().add(0.0, 0.3, 0.0), landing.add(0.0, 0.3, 0.0),
				ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() != HitResult.Type.MISS) {
			return;
		}

		this.wildways$landingPosition = landing;
		this.wildways$windupTicks = WILDWAYS_WINDUP_TICKS;
		this.wildways$dashCooldown = 120 + this.random.nextInt(81);
		this.playSound(SoundEvents.WITHER_AMBIENT, 2.0F, 0.65F);
	}

	@Inject(method = "performRangedAttack(IDDDZ)V", at = @At("HEAD"), cancellable = true)
	private void wildways$pauseSkullShots(int head, double x, double y, double z, boolean dangerous, CallbackInfo ci) {
		if (this.wildways$isDashActive()) {
			ci.cancel();
		}
	}

	@Override
	public void travel(Vec3 input) {
		if (!(this.level() instanceof ServerLevel level) || !this.wildways$isDashActive()) {
			super.travel(input);
			return;
		}

		this.getNavigation().stop();
		this.setDeltaMovement(Vec3.ZERO);
		if (this.wildways$recoveryTicks > 0) {
			this.wildways$recoveryTicks--;
			// Stay at ground level and fall normally if the floor disappears.
			this.move(MoverType.SELF, new Vec3(0.0, -0.12, 0.0));
			return;
		}
		if (this.wildways$windupTicks > 0) {
			Vec3 landing = this.wildways$landingPosition;
			if (this.wildways$windupTicks % 4 == 0) {
				level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, landing.x, landing.y + 0.15, landing.z, 12, 0.7, 0.1, 0.7, 0.01);
			}
			this.wildways$diving = --this.wildways$windupTicks == 0;
			return;
		}

		Vec3 offset = this.wildways$landingPosition.subtract(this.position());
		Vec3 movement = offset.lengthSqr() > WILDWAYS_DASH_SPEED * WILDWAYS_DASH_SPEED
			? offset.normalize().scale(WILDWAYS_DASH_SPEED)
			: offset.add(0.0, -0.05, 0.0);
		this.move(MoverType.SELF, movement);
		if (this.verticalCollisionBelow) {
			this.wildways$diving = false;
			this.wildways$recoveryTicks = WILDWAYS_RECOVERY_TICKS;
			this.wildways$dashImpact(level);
		} else if (this.horizontalCollision || this.getY() <= this.wildways$landingPosition.y) {
			this.wildways$diving = false;
		}
	}

	@Unique
	private boolean wildways$isDashActive() {
		return this.wildways$windupTicks > 0 || this.wildways$diving || this.wildways$recoveryTicks > 0;
	}

	@Unique
	private void wildways$dashImpact(ServerLevel level) {
		level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.2, this.getZ(), 20, 0.8, 0.1, 0.8, 0.06);
		this.playSound(SoundEvents.WITHER_HURT, 1.5F, 0.65F);
		for (Player player : level.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(WILDWAYS_IMPACT_RADIUS, 0.5, WILDWAYS_IMPACT_RADIUS))) {
			if (this.canAttack(player) && this.distanceToSqr(player) <= WILDWAYS_IMPACT_RADIUS * WILDWAYS_IMPACT_RADIUS && this.hasLineOfSight(player)) {
				player.hurtServer(level, this.damageSources().mobAttack(this), 8.0F);
			}
		}
	}
}
