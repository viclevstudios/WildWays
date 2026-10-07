package com.viclev.wildways.mixin;

import java.util.function.BiConsumer;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemCombinerMenu.class)
public class ItemCombinerRetentionMixin {
	@Redirect(method = "removed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ContainerLevelAccess;execute(Ljava/util/function/BiConsumer;)V"))
	private void wildways$keepAnvilInputs(ContainerLevelAccess access, BiConsumer<Level, BlockPos> action) {
		if (!((Object)this instanceof AnvilMenu)) access.execute(action);
	}
}
