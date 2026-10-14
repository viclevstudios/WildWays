package com.viclev.wildways.mixin;

import com.viclev.wildways.ModItems;
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
public abstract class VolcaniteToolRangeMixin {
	@Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V", at = @At("TAIL"))
	private void wildways$addBlockReach(EquipmentSlotGroup slot, TriConsumer consumer, CallbackInfo ci) {
		ItemStack stack = (ItemStack) (Object) this;
		if (slot == EquipmentSlotGroup.MAINHAND && ModItems.VOLCANITE_TOOLS.contains(stack.getItem())) {
			consumer.accept(Attributes.BLOCK_INTERACTION_RANGE,
				new AttributeModifier(Wildways.id("volcanite_tool_block_range"), ProgressionMaterials.VOLCANITE_RANGE_PER_PIECE, AttributeModifier.Operation.ADD_VALUE),
				ItemAttributeModifiers.Display.attributeModifiers());
		}
	}
}
