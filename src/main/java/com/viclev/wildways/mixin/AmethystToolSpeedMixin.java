package com.viclev.wildways.mixin;

import com.viclev.wildways.ProgressionItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class AmethystToolSpeedMixin {
	// Apply the bonus after Efficiency, while terrain and status effects still scale it normally.
	@ModifyVariable(method = "getDestroySpeed", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/world/effect/MobEffectUtil;hasDigSpeed(Lnet/minecraft/world/entity/LivingEntity;)Z"), ordinal = 0)
	private float wildways$boostIneffectiveAmethystMining(float speed) {
		if (speed <= 1.0F) {
			ItemStack stack = ((Player) (Object) this).getMainHandItem();
			if (ProgressionItems.isAmethystTool(stack.getItem()) || ProgressionItems.hasAmethystHeritage(stack)) return 1.5F;
		}
		return speed;
	}
}
