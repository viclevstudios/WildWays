package com.viclev.wildways.client.mixin;

import com.viclev.wildways.client.HeldItemInfoHud;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudMixin {
	@Shadow private int overlayMessageTime;
	@Shadow public abstract Font getFont();

	@Inject(method = "extractOverlayMessage", at = @At("TAIL"))
	private void wildways$drawHeldItemInformation(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
		Component information = HeldItemInfoHud.information();
		if (this.overlayMessageTime <= 0 && information != null) {
			Font font = this.getFont();
			int width = font.width(information);
			graphics.nextStratum();
			graphics.textWithBackdrop(font, information, (graphics.guiWidth() - width) / 2, graphics.guiHeight() - 64, width, 0xFFFFFFFF);
		}
	}
}
