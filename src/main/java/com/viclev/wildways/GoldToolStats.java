package com.viclev.wildways;

import java.util.List;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.level.block.Block;

/** Rebalances the actual vanilla gold tools, including existing loot and traded items. */
public final class GoldToolStats {
	private GoldToolStats() {
	}

	public static void initialize() {
		DefaultItemComponentEvents.MODIFY.register(context -> context.modify(ProgressionItems.GOLD_TOOLS, (components, lookup, item) -> {
			components.set(DataComponents.MAX_DAMAGE, ProgressionMaterials.GOLD_TOOL_DURABILITY);
			components.set(DataComponents.ENCHANTABLE, new Enchantable(ProgressionMaterials.GOLD_TOOL.enchantmentValue()));
			components.set(DataComponents.ATTRIBUTE_MODIFIERS, attributes(item));
			if (item != Items.GOLDEN_SWORD) {
				var blocks = lookup.lookupOrThrow(Registries.BLOCK);
				components.set(DataComponents.TOOL, new Tool(List.of(
					Tool.Rule.deniesDrops(blocks.getOrThrow(ProgressionMaterials.INCORRECT_GOLD)),
					Tool.Rule.minesAndDrops(blocks.getOrThrow(efficientBlocks(item)), ProgressionMaterials.GOLD_TOOL_SPEED)),
					1.0F, 1, true));
			}
		}));
	}

	private static TagKey<Block> efficientBlocks(Item item) {
		if (item == Items.GOLDEN_PICKAXE) return BlockTags.MINEABLE_WITH_PICKAXE;
		if (item == Items.GOLDEN_AXE) return BlockTags.MINEABLE_WITH_AXE;
		if (item == Items.GOLDEN_SHOVEL) return BlockTags.MINEABLE_WITH_SHOVEL;
		return BlockTags.MINEABLE_WITH_HOE;
	}

	private static ItemAttributeModifiers attributes(Item item) {
		float damage;
		float speed;
		if (item == Items.GOLDEN_SWORD) {
			damage = ProgressionMaterials.SWORD_ATTACK_DAMAGE;
			speed = ProgressionMaterials.SWORD_ATTACK_SPEED;
		} else if (item == Items.GOLDEN_PICKAXE) {
			damage = ProgressionMaterials.PICKAXE_ATTACK_DAMAGE;
			speed = ProgressionMaterials.PICKAXE_ATTACK_SPEED;
		} else if (item == Items.GOLDEN_AXE) {
			damage = ProgressionMaterials.AXE_ATTACK_DAMAGE;
			speed = ProgressionMaterials.AXE_ATTACK_SPEED;
		} else if (item == Items.GOLDEN_SHOVEL) {
			damage = ProgressionMaterials.SHOVEL_ATTACK_DAMAGE;
			speed = ProgressionMaterials.SHOVEL_ATTACK_SPEED;
		} else {
			damage = ProgressionMaterials.HOE_ATTACK_DAMAGE;
			speed = ProgressionMaterials.HOE_ATTACK_SPEED;
		}
		return ItemAttributeModifiers.builder()
			.add(Attributes.ATTACK_DAMAGE,
				new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, damage + ProgressionMaterials.GOLD_ATTACK_DAMAGE_BONUS, AttributeModifier.Operation.ADD_VALUE),
				EquipmentSlotGroup.MAINHAND)
			.add(Attributes.ATTACK_SPEED,
				new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, speed, AttributeModifier.Operation.ADD_VALUE),
				EquipmentSlotGroup.MAINHAND)
			.build();
	}
}
