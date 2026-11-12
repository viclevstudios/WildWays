package com.viclev.wildways;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

final class AnimalWeightsQuests {
	private static final String MOD_ID = "animalweights";
	private static final String PACKAGE = "com.leclowndu93150.animalweights.";
	private static final Identifier GLASS = Identifier.fromNamespaceAndPath(MOD_ID, "magnifying_glass");
	private static final TagKey<EntityType<?>> LIVESTOCK = TagKey.create(Registries.ENTITY_TYPE,
		Identifier.fromNamespaceAndPath(MOD_ID, "livestock"));
	private static final Api API = findApi();

	private AnimalWeightsQuests() {
	}

	static boolean isAvailable() {
		return API != null;
	}

	static void initialize() {
		if (!isAvailable()) {
			return;
		}
		UseEntityCallback.EVENT.register((player, level, hand, target, hit) -> {
			if (player instanceof ServerPlayer serverPlayer && !player.isSpectator()
				&& target instanceof Animal animal && animal.isAlive() && animal.typeHolder().is(LIVESTOCK)
				&& GLASS.equals(BuiltInRegistries.ITEM.getKey(player.getItemInHand(hand).getItem()))) {
				checkHabitat(serverPlayer, animal);
			}
			return InteractionResult.PASS;
		});
		ServerLivingEntityEvents.AFTER_DEATH.register((target, source) -> {
			if (target instanceof Animal animal && !animal.isBaby() && animal.typeHolder().is(LIVESTOCK)
				&& source.getEntity() instanceof ServerPlayer player) {
				try {
					if (API.tracks(animal) && (int)API.weight.invoke(null, animal) == 8) {
						QuestBookItem.awardQuest(player, Wildways.id("quests/hunt_heavy_livestock"));
					}
				} catch (ReflectiveOperationException exception) {
					Wildways.LOGGER.warn("Could not read Animal Weights livestock weight", exception);
				}
			}
		});
	}

	private static void checkHabitat(ServerPlayer player, Animal animal) {
		try {
			if (!API.tracks(animal)) {
				return;
			}
			var level = animal.level();
			var pos = animal.blockPosition();
			var diet = API.diet.invoke(null, animal);
			var config = API.config.invoke(null);
			boolean water = (boolean)API.naturalWater.invoke(null, level, pos, diet)
				|| API.waterCauldron.invoke(null, level, pos, diet) != null;
			if (water && (boolean)API.brightLight.invoke(null, level, pos)
				&& (boolean)API.grazing.invoke(null, level, pos, API.scanRadius.getInt(config))
				&& (boolean)API.space.invoke(null, animal, level, config)) {
				QuestBookItem.awardQuest(player, Wildways.id("quests/perfect_habitat"));
			}
		} catch (ReflectiveOperationException exception) {
			Wildways.LOGGER.warn("Could not inspect Animal Weights livestock habitat", exception);
		}
	}

	// Resolve Animal Weights 1.2.2's methods only when the optional mod is installed.
	private static Api findApi() {
		if (!FabricLoader.getInstance().isModLoaded(MOD_ID)) {
			return null;
		}
		try {
			var rules = Class.forName(PACKAGE + "AnimalWeightsRules");
			var scanner = Class.forName(PACKAGE + "habitat.HabitatScanner");
			var tickLogic = Class.forName(PACKAGE + "habitat.WeightTickLogic");
			var diet = Class.forName(PACKAGE + "config.Diet");
			var config = Class.forName(PACKAGE + "config.AnimalWeightsConfig");
			return new Api(
				rules.getMethod("isActive", Animal.class),
				rules.getMethod("isDisabled", Entity.class),
				rules.getMethod("dietOf", Entity.class),
				Class.forName(PACKAGE + "WeightAttachment").getMethod("getWeight", Animal.class),
				Class.forName(PACKAGE + "config.ConfigManager").getMethod("get"),
				config.getField("habitatScanRadius"),
				scanner.getMethod("hasUsableNaturalWater", Level.class, BlockPos.class, diet),
				scanner.getMethod("findUsableCauldron", Level.class, BlockPos.class, diet),
				scanner.getMethod("hasBrightLight", Level.class, BlockPos.class),
				scanner.getMethod("hasGrazingNearby", Level.class, BlockPos.class, int.class),
				tickLogic.getMethod("hasSpace", Animal.class, Level.class, config)
			);
		} catch (ReflectiveOperationException | LinkageError exception) {
			Wildways.LOGGER.warn("Animal Weights quest integration is unavailable", exception);
			return null;
		}
	}

	private record Api(Method active, Method disabled, Method diet, Method weight, Method config, Field scanRadius,
		Method naturalWater, Method waterCauldron, Method brightLight, Method grazing, Method space) {
		boolean tracks(Animal animal) throws ReflectiveOperationException {
			return !(boolean)this.disabled.invoke(null, animal) && (boolean)this.active.invoke(null, animal);
		}
	}
}
