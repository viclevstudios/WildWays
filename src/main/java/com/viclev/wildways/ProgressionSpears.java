package com.viclev.wildways;

import java.util.Optional;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.KineticWeapon;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.item.enchantment.Enchantable;

/** Vanilla spear mechanics with the same material traits as the other progression tools. */
public final class ProgressionSpears {
	private static final float IRON_ATTACK_DURATION = 0.95F;

	private ProgressionSpears() {
	}

	public static Item.Properties quartzProperties() {
		return new Item.Properties().spear(ProgressionMaterials.QUARTZ_TOOL,
			IRON_ATTACK_DURATION, 0.95F, 0.6F, 2.5F, 11.0F, 6.75F, 5.1F, 11.25F, 4.6F);
	}

	public static Item.Properties amethystProperties() {
		float duration = 1.0F / (1.0F / IRON_ATTACK_DURATION + ProgressionMaterials.AMETHYST_ATTACK_SPEED_BONUS);
		return new Item.Properties().spear(ProgressionMaterials.AMETHYST_TOOL,
			duration, 0.95F, 0.6F, 2.5F, 11.0F, 6.75F, 5.1F, 11.25F, 4.6F);
	}

	public static Item.Properties volcaniteProperties() {
		float bonus = (float) ProgressionMaterials.VOLCANITE_RANGE_PER_PIECE;
		return ProgressionItems.volcaniteProperties().spear(ProgressionMaterials.VOLCANITE_TOOL,
			1.05F, 1.075F, 0.5F, 3.0F, 10.0F, 6.5F, 5.1F, 10.0F, 4.6F)
			.component(DataComponents.ATTACK_RANGE, new AttackRange(2.0F, 4.5F + bonus, 2.0F, 6.5F + bonus, 0.125F, 0.5F));
	}

	public static void initialize() {
		// Modify the vanilla item so existing spears, loot and trades all use the new gold tier.
		DefaultItemComponentEvents.MODIFY.register(context -> context.modify(Items.GOLDEN_SPEAR, components -> {
			components.set(DataComponents.MAX_DAMAGE, ProgressionMaterials.GOLD_TOOL_DURABILITY);
			components.set(DataComponents.ENCHANTABLE, new Enchantable(ProgressionMaterials.GOLD_TOOL.enchantmentValue()));
			components.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
				.add(Attributes.ATTACK_DAMAGE,
					new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, ProgressionMaterials.GOLD_ATTACK_DAMAGE_BONUS, AttributeModifier.Operation.ADD_VALUE),
					EquipmentSlotGroup.MAINHAND)
				.add(Attributes.ATTACK_SPEED,
					new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -3.0, AttributeModifier.Operation.ADD_VALUE),
					EquipmentSlotGroup.MAINHAND)
				.build());
			components.set(DataComponents.SWING_ANIMATION, new SwingAnimation(SwingAnimationType.STAB, 20));
			// Midpoints of the iron and diamond spear profiles, including the charged attack.
			components.set(DataComponents.KINETIC_WEAPON, new KineticWeapon(10, 11,
				KineticWeapon.Condition.ofAttackerSpeed(55, 10.5F),
				KineticWeapon.Condition.ofAttackerSpeed(132, 5.1F),
				KineticWeapon.Condition.ofRelativeSpeed(212, 4.6F),
				0.38F, 1.0125F, Optional.of(SoundEvents.SPEAR_USE), Optional.of(SoundEvents.SPEAR_HIT)));
		}));
	}
}
