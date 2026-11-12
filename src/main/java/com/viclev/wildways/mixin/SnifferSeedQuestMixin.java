package com.viclev.wildways.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Sniffer.class)
abstract class SnifferSeedQuestMixin {
	@ModifyExpressionValue(method = "lambda$dropSeed$0", at = @At(value = "NEW", target = "net/minecraft/world/entity/item/ItemEntity"))
	private ItemEntity wildways$recordSeedSource(ItemEntity drop) {
		drop.setThrower((Sniffer)(Object)this);
		return drop;
	}
}
