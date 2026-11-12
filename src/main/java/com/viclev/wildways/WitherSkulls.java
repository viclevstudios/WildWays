package com.viclev.wildways;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.WitherSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class WitherSkulls {
	public static final Variant SCULK = register("sculk_covered");
	public static final Variant GOLD = register("gold_adorned");

	private WitherSkulls() {
	}

	private static Variant register(String name) {
		Identifier id = Wildways.id(name + "_wither_skull");
		SkullType type = new SkullType(id);
		SkullBlock.Type.TYPES.put(type.getSerializedName(), type);
		Block skull = Registry.register(BuiltInRegistries.BLOCK, id, new VariantSkullBlock(type,
			BlockBehaviour.Properties.ofFullCopy(Blocks.WITHER_SKELETON_SKULL).setId(ResourceKey.create(Registries.BLOCK, id))));
		Identifier wallId = id.withPath(path -> path.replace("_skull", "_wall_skull"));
		Block wallSkull = Registry.register(BuiltInRegistries.BLOCK, wallId, new VariantWallSkullBlock(type,
			BlockBehaviour.Properties.ofFullCopy(Blocks.WITHER_SKELETON_WALL_SKULL).setId(ResourceKey.create(Registries.BLOCK, wallId))
				.overrideDescription(skull.getDescriptionId()).overrideLootTable(skull.getLootTable())));
		Item item = Registry.register(BuiltInRegistries.ITEM, id, new StandingAndWallBlockItem(skull, wallSkull, Direction.DOWN,
			new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).useBlockDescriptionPrefix()
				.rarity(Rarity.RARE).equippableUnswappable(EquipmentSlot.HEAD)));
		return new Variant(skull, wallSkull, item);
	}

	public static void initialize() {
		FabricBlockEntityType skullEntityType = (FabricBlockEntityType) BlockEntityTypes.SKULL;
		for (Variant variant : new Variant[] {SCULK, GOLD}) {
			skullEntityType.addValidBlock(variant.skull());
			skullEntityType.addValidBlock(variant.wallSkull());
		}
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> {
			output.accept(SCULK.item());
			output.accept(GOLD.item());
		});
	}

	public record Variant(Block skull, Block wallSkull, Item item) {
		public boolean matches(BlockState state) {
			return state.is(skull) || state.is(wallSkull);
		}
	}

	public record SkullType(Identifier id) implements SkullBlock.Type {
		@Override
		public String getSerializedName() {
			return id.toString();
		}
	}

	private static final class VariantSkullBlock extends SkullBlock {
		private VariantSkullBlock(SkullType type, Properties properties) {
			super(type, properties);
		}

		@Override
		public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
			WitherSkullBlock.checkSpawn(level, pos);
		}
	}

	private static final class VariantWallSkullBlock extends WallSkullBlock {
		private VariantWallSkullBlock(SkullType type, Properties properties) {
			super(type, properties);
		}

		@Override
		public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
			WitherSkullBlock.checkSpawn(level, pos);
		}
	}
}
