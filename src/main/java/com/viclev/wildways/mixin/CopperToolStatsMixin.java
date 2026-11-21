package com.viclev.wildways.mixin;

import com.viclev.wildways.ProgressionItems;
import com.viclev.wildways.ProgressionMaterials;
import com.viclev.wildways.Wildways;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.function.TriConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiConsumer;

@Mixin(ItemStack.class)
public abstract class CopperToolStatsMixin {
	@Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
	private void wildways$adjustMiningSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
		float speed = cir.getReturnValue();
		if (speed == ToolMaterial.COPPER.speed() && isCopperTool()) {
			cir.setReturnValue(ToolMaterial.GOLD.speed());
		} else if (speed > 1.0F) {
			int penalties = ProgressionItems.strengthPenalties((ItemStack) (Object) this);
			if (penalties > 0) {
				cir.setReturnValue(Math.max(1.0F, speed - penalties * ProgressionMaterials.MINING_SPEED_PENALTY));
			}
		}
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
	private void wildways$showGoldArmorBonus(EquipmentSlotGroup slot,
			TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display> consumer, CallbackInfo ci) {
		this.wildways$addGoldArmorBonus(slot, (attribute, modifier) ->
			consumer.accept(attribute, modifier, ItemAttributeModifiers.Display.attributeModifiers()));
	}

	@Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", at = @At("TAIL"))
	private void wildways$equipGoldArmorBonus(EquipmentSlot slot, BiConsumer<Holder<Attribute>, AttributeModifier> consumer, CallbackInfo ci) {
		this.wildways$addGoldArmorBonus(EquipmentSlotGroup.bySlot(slot), consumer);
	}

	@Unique
	private void wildways$addGoldArmorBonus(EquipmentSlotGroup slot, BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
		String id = itemPath();
		int bonus = switch (id) {
			case "golden_chestplate" -> slot == EquipmentSlotGroup.CHEST ? 3 : 0;
			case "golden_leggings" -> slot == EquipmentSlotGroup.LEGS ? 3 : 0;
			case "golden_boots" -> slot == EquipmentSlotGroup.FEET ? 1 : 0;
			default -> 0;
		};
		if (bonus > 0) {
			consumer.accept(Attributes.ARMOR, new AttributeModifier(Wildways.id("gold_armor_progression_" + id), bonus,
				AttributeModifier.Operation.ADD_VALUE));
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
