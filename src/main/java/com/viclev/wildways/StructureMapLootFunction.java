package com.viclev.wildways;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.Set;

final class StructureMapLootFunction extends LootItemConditionalFunction {
	static final MapCodec<StructureMapLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> commonFields(instance)
		.and(instance.group(
			Item.CODEC.fieldOf("map_item").forGetter(function -> function.mapItem),
			LootItemFunctions.CODEC.optionalFieldOf("structure_locator").forGetter(function -> function.structureLocator),
			Codec.BOOL.optionalFieldOf("keep_original", false).forGetter(function -> function.keepOriginal)
		)).apply(instance, StructureMapLootFunction::new));

	private final Holder<Item> mapItem;
	// Omit the vanilla locator for dungeons, which are indexed separately from structures.
	private final Optional<Holder<LootItemFunction>> structureLocator;
	private final boolean keepOriginal;

	StructureMapLootFunction(Optional<Holder<LootItemCondition>> condition, Holder<Item> mapItem,
		Optional<Holder<LootItemFunction>> structureLocator, boolean keepOriginal) {
		super(condition);
		this.mapItem = mapItem;
		this.structureLocator = structureLocator;
		this.keepOriginal = keepOriginal;
	}

	@Override
	public MapCodec<StructureMapLootFunction> codec() {
		return CODEC;
	}

	@Override
	public Set<ContextKey<?>> getReferencedContextParams() {
		return Set.of(LootContextParams.ORIGIN);
	}

	@Override
	public void validate(ValidationContext context) {
		super.validate(context);
		Validatable.validateHolder(context, "structure_locator", this.structureLocator);
	}

	@Override
	public ItemStack run(ItemStack original, LootContext context) {
		ItemStack map = new ItemStack(this.mapItem);
		if (this.structureLocator.isPresent()) {
			map = this.structureLocator.get().value().apply(map, context);
		} else {
			Vec3 origin = context.getOptional(LootContextParams.ORIGIN);
			ServerLevel level = context.getLevel();
			BlockPos dungeon = origin == null ? null : DungeonLocations.findNearest(level, BlockPos.containing(origin));
			if (dungeon != null) {
				MapItem.applyNewSavedData(level, map, dungeon.getX(), dungeon.getZ(), (byte)1, true, true);
				MapItem.renderBiomePreviewMap(level, map);
				MapItemSavedData.addTargetDecoration(map, dungeon, "+", MapDecorationTypes.RED_X);
			}
		}
		return map.has(DataComponents.MAP_ID) ? map : this.keepOriginal ? original : ItemStack.EMPTY;
	}
}
