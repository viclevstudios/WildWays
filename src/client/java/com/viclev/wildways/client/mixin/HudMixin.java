package com.viclev.wildways.client.mixin;

import com.viclev.wildways.client.HeldItemInfoHud;
import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Hud.class)
public class HudMixin {
	@Shadow
	private Component overlayMessageString;

	@ModifyConstant(method = "extractOverlayMessage", constant = @Constant(intValue = 68))
	private int wildways$lowerHeldItemInformation(int actionBarOffset) {
		return HeldItemInfoHud.isInformationMessage(this.overlayMessageString) ? actionBarOffset - 8 : actionBarOffset;
	}
}
