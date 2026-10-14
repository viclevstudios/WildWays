package com.viclev.wildways.mixin;

import com.viclev.wildways.ProgressionItems;
import com.viclev.wildways.ProgressionMaterials;
import com.viclev.wildways.Wildways;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.apache.commons.lang3.function.TriConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class AmethystHeritageAttributesMixin {
	@Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V", at = @At("TAIL"))
	private void wildways$restoreAmethystAttribute(EquipmentSlotGroup slot, TriConsumer consumer, CallbackInfo ci) {
		ItemStack stack = (ItemStack) (Object) this;
		if (!ProgressionItems.hasAmethystHeritage(stack)) return;
		if (slot == EquipmentSlotGroup.MAINHAND && ProgressionItems.isToolEquipment(stack.getItem())) {
			consumer.accept(Attributes.ATTACK_SPEED,
				new AttributeModifier(Wildways.id("amethyst_heritage_attack_speed"), ProgressionMaterials.AMETHYST_ATTACK_SPEED_BONUS, AttributeModifier.Operation.ADD_VALUE),
				ItemAttributeModifiers.Display.attributeModifiers());
		}
		if (slot != EquipmentSlotGroup.MAINHAND && ProgressionItems.isArmorEquipment(stack.getItem())) {
			consumer.accept(Attributes.MOVEMENT_SPEED,
				new AttributeModifier(Wildways.id("amethyst_heritage_movement_speed"), ProgressionMaterials.AMETHYST_SPEED_PER_PIECE, AttributeModifier.Operation.ADD_VALUE),
				ItemAttributeModifiers.Display.attributeModifiers());
		}
	}
}
