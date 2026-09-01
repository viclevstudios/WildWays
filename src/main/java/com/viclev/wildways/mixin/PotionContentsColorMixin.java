package com.viclev.wildways.mixin;

import com.viclev.wildways.ModPotions;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/** Keeps each upgrade's bottle and tipped-arrow tint identical to its base potion. */
@Mixin(PotionContents.class)
public class PotionContentsColorMixin {
	@Inject(method = "createItemStack", at = @At("RETURN"), cancellable = true)
	private static void wildways$giveCreatedPotionItsVanillaTint(Item item, Holder<Potion> potion, CallbackInfoReturnable<ItemStack> cir) {
		wildways$applyVanillaTint(cir.getReturnValue(), potion);
	}

	@Inject(method = "withPotion", at = @At("RETURN"), cancellable = true)
	private void wildways$keepVanillaTintWhenBrewing(Holder<Potion> potion, CallbackInfoReturnable<PotionContents> cir) {
		ModPotions.visualBase(potion).ifPresent(base -> {
			PotionContents result = cir.getReturnValue();
			cir.setReturnValue(new PotionContents(
				Optional.of(potion),
				Optional.of(new PotionContents(base).getColor()),
				result.customEffects(),
				result.customName()
			));
		});
	}

	@Inject(method = "getColor", at = @At("RETURN"), cancellable = true)
	private void wildways$useBasePotionColor(CallbackInfoReturnable<Integer> cir) {
		PotionContents contents = (PotionContents) (Object) this;
		contents.potion()
			.flatMap(ModPotions::visualBase)
			.ifPresent(base -> cir.setReturnValue(new PotionContents(base).getColor()));
	}

	private static void wildways$applyVanillaTint(ItemStack stack, Holder<Potion> potion) {
		ModPotions.visualBase(potion).ifPresent(base -> stack.set(
			DataComponents.POTION_CONTENTS,
			new PotionContents(Optional.of(potion), Optional.of(new PotionContents(base).getColor()), java.util.List.of(), Optional.empty())
		));
	}
}
