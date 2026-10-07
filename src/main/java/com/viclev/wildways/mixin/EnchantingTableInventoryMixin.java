package com.viclev.wildways.mixin;

import com.viclev.wildways.WorkstationInventory;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantingTableBlockEntity.class)
public abstract class EnchantingTableInventoryMixin extends BlockEntity implements WorkstationInventory {
	@Unique private final NonNullList<ItemStack> wildways$storedItems = NonNullList.withSize(5, ItemStack.EMPTY);

	private EnchantingTableInventoryMixin() { super(null, null, null); }

	@Override public NonNullList<ItemStack> wildways$items() { return this.wildways$storedItems; }
	@Override public BlockEntity wildways$blockEntity() { return this; }
	@Override public void setChanged() {
		super.setChanged();
		if (this.level != null && !this.level.isClientSide()) {
			this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
		}
	}

	@Inject(method = "saveAdditional", at = @At("TAIL"))
	private void wildways$saveItems(ValueOutput output, CallbackInfo ci) { this.wildways$save(output); }

	@Inject(method = "loadAdditional", at = @At("TAIL"))
	private void wildways$loadItems(ValueInput input, CallbackInfo ci) { this.wildways$load(input); }

	@Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
	@Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return this.saveWithoutMetadata(registries); }
}
