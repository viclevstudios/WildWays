package com.viclev.wildways.mixin;

import com.viclev.wildways.ProgressionItems;
import com.viclev.wildways.ProgressionMaterials;
import com.viclev.wildways.Wildways;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;
import org.apache.commons.lang3.function.TriConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(ItemStack.class)
public abstract class AmethystHeritageAttributesMixin {
	@Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V", at = @At("TAIL"))
	private void wildways$showMaterialAttributes(EquipmentSlotGroup slot,
			TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display> consumer, CallbackInfo ci) {
		this.wildways$addMaterialAttributes(slot, (attribute, modifier) ->
			consumer.accept(attribute, modifier, ItemAttributeModifiers.Display.attributeModifiers()));
	}

	@Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", at = @At("TAIL"))
	private void wildways$equipMaterialAttributes(EquipmentSlot slot,
			BiConsumer<Holder<Attribute>, AttributeModifier> consumer, CallbackInfo ci) {
		this.wildways$addMaterialAttributes(EquipmentSlotGroup.bySlot(slot), consumer);
	}

	@Unique
	private void wildways$addMaterialAttributes(EquipmentSlotGroup slot, BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
		ItemStack stack = (ItemStack) (Object) this;
		boolean amethyst = ProgressionItems.hasAmethystHeritage(stack);
		boolean gold = ProgressionItems.hasGoldHeritage(stack);
		if (!amethyst && !gold) return;

		if (slot == EquipmentSlotGroup.MAINHAND && ProgressionItems.isToolEquipment(stack.getItem())) {
			if (amethyst) consumer.accept(Attributes.ATTACK_SPEED, new AttributeModifier(Wildways.id("amethyst_heritage_attack_speed"),
				ProgressionMaterials.AMETHYST_ATTACK_SPEED_BONUS, AttributeModifier.Operation.ADD_VALUE));
			if (gold) consumer.accept(Attributes.ATTACK_DAMAGE, new AttributeModifier(Wildways.id("gold_heritage_attack_damage"),
				ProgressionMaterials.GOLD_HERITAGE_ATTACK_DAMAGE, AttributeModifier.Operation.ADD_VALUE));
		}
		Equippable equipment = stack.get(DataComponents.EQUIPPABLE);
		if (equipment != null && slot == EquipmentSlotGroup.bySlot(equipment.slot()) && ProgressionItems.isArmorEquipment(stack.getItem())) {
			String suffix = equipment.slot().getName();
			if (amethyst) consumer.accept(Attributes.MOVEMENT_SPEED, new AttributeModifier(Wildways.id("amethyst_heritage_movement_speed_" + suffix),
				ProgressionMaterials.AMETHYST_SPEED_PER_PIECE, AttributeModifier.Operation.ADD_VALUE));
			if (gold) consumer.accept(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(Wildways.id("gold_heritage_toughness_" + suffix),
				ProgressionMaterials.GOLD_HERITAGE_ARMOR_TOUGHNESS, AttributeModifier.Operation.ADD_VALUE));
		}
	}
}
