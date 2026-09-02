package com.viclev.wildways.mixin;

import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Accesses vanilla launch state when creating a custom arrow entity type. */
@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor {
	@Accessor("firedFromWeapon")
	void wildways$setFiredFromWeapon(ItemStack weapon);

	@Invoker("setPierceLevel")
	void wildways$setPierceLevel(byte pierceLevel);
}
