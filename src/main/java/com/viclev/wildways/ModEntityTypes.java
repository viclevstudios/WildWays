package com.viclev.wildways;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/** Registers separate network entity types so each special arrow can render independently. */
public final class ModEntityTypes {
	public static final EntityType<TurtleArrowEntity> TURTLE_ARROW = register("turtle_arrow", TurtleArrowEntity::new);
	public static final EntityType<RangeArrowEntity> RANGE_ARROW = register("range_arrow", RangeArrowEntity::new);
	public static final EntityType<ExplosiveArrowEntity> EXPLOSIVE_ARROW = register("explosive_arrow", ExplosiveArrowEntity::new);

	private ModEntityTypes() {
	}

	private static <T extends WildwaysArrowEntity> EntityType<T> register(String name, EntityType.EntityFactory<T> factory) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Wildways.id(name));
		EntityType<T> type = EntityType.Builder.of(factory, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(4)
			.updateInterval(20)
			.build(key);
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type);
	}

	public static void initialize() {
	}
}
