package com.viclev.wildways.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.function.TriConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class CopperToolStatsMixin {
	@Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
	private void wildways$goldSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
		if (isCopperTool()) cir.setReturnValue(12.0F);
	}

	@Inject(method = "getMaxDamage", at = @At("RETURN"), cancellable = true)
	private void wildways$lowerDurability(CallbackInfoReturnable<Integer> cir) {
		if (isCopperTool()) cir.setReturnValue(100);
		else {
			String id = itemPath();
			int durability = switch (id) {
				case "golden_boots" -> 247;
				case "golden_leggings" -> 285;
				case "golden_chestplate" -> 304;
				case "golden_helmet" -> 209;
				default -> -1;
			};
			if (durability > 0) cir.setReturnValue(durability);
		}
	}

	@Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V", at = @At("TAIL"))
	private void wildways$improveVanillaGoldArmor(EquipmentSlotGroup slot, TriConsumer consumer, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
		String id = itemPath();
		int bonus = switch (id) {
			case "golden_chestplate" -> slot == EquipmentSlotGroup.CHEST ? 2 : 0;
			case "golden_leggings" -> slot == EquipmentSlotGroup.LEGS ? 2 : 0;
			case "golden_boots" -> slot == EquipmentSlotGroup.FEET ? 1 : 0;
			default -> 0;
		};
		if (bonus > 0) {
			consumer.accept(Attributes.ARMOR, new AttributeModifier(net.minecraft.resources.Identifier.fromNamespaceAndPath("wildways", "gold_armor_progression"), bonus, AttributeModifier.Operation.ADD_VALUE), ItemAttributeModifiers.Display.attributeModifiers());
		}
	}

	private boolean isCopperTool() {
		String id = itemPath();
		return id.startsWith("copper_") && (id.endsWith("_sword") || id.endsWith("_pickaxe") || id.endsWith("_axe") || id.endsWith("_shovel") || id.endsWith("_hoe"));
	}

	private String itemPath() {
		return BuiltInRegistries.ITEM.getKey(((ItemStack) (Object) this).getItem()).getPath();
	}
}
