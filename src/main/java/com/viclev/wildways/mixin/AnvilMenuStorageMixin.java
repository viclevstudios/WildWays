package com.viclev.wildways.mixin;

import com.viclev.wildways.WorkstationInventory;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuStorageMixin extends ItemCombinerMenu {
	@Unique private boolean wildways$loadingStoredItems;
	protected AnvilMenuStorageMixin(MenuType<?> type, int id, Inventory inventory, ContainerLevelAccess access,
		ItemCombinerMenuSlotDefinition slots) {
		super(type, id, inventory, access, slots);
	}

	@Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
	private void wildways$loadStoredInputs(int id, Inventory inventory, ContainerLevelAccess access, CallbackInfo ci) {
		if (!inventory.player.level().isClientSide()) {
			access.execute((level, pos) -> {
				if (level.getBlockEntity(pos) instanceof WorkstationInventory stored) {
					this.wildways$loadingStoredItems = true;
					for (int i = 0; i < 2; i++) this.inputSlots.setItem(i, stored.getItem(i).copy());
					this.wildways$loadingStoredItems = false;
					this.createResult();
				}
			});
		}
	}

	@Override
	public void slotsChanged(Container container) {
		super.slotsChanged(container);
		if (container == this.inputSlots && !this.player.level().isClientSide() && !this.wildways$loadingStoredItems) {
			this.access.execute((level, pos) -> {
				if (level.getBlockEntity(pos) instanceof WorkstationInventory stored) {
					for (int i = 0; i < 2; i++) stored.setItem(i, this.inputSlots.getItem(i).copy());
				}
			});
		}
	}
}
