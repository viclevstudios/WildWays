package com.viclev.wildways.mixin;

import com.viclev.wildways.EnchantedBookSources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** One ordinary dropped book becomes a Channeling recipe when struck. */
@Mixin(Entity.class)
public abstract class BookLightningMixin {
	@Inject(method = "thunderHit", at = @At("TAIL"))
	private void wildways$channelingBook(ServerLevel level, LightningBolt lightning, CallbackInfo ci) {
		if (!((Object)this instanceof ItemEntity item) || !item.getItem().is(Items.BOOK)
			|| item.entityTags().contains("wildways:channeling_from_lightning")) {
			return;
		}
		ItemStack stack = item.getItem();
		ItemStack book = EnchantedBookSources.book(level, Enchantments.CHANNELING);
		if (stack.getCount() == 1) {
			item.setItem(book);
		} else {
			ItemStack remaining = stack.copy();
			remaining.shrink(1);
			item.setItem(book);
			item.spawnAtLocation(level, remaining);
		}
		item.addTag("wildways:channeling_from_lightning");
	}
}
