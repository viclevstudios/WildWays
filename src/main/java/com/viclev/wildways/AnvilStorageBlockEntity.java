package com.viclev.wildways;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class AnvilStorageBlockEntity extends BlockEntity implements WorkstationInventory {
	private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);

	public AnvilStorageBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.ANVIL_STORAGE, pos, state);
	}

	@Override public NonNullList<ItemStack> wildways$items() { return this.items; }
	@Override public BlockEntity wildways$blockEntity() { return this; }
	@Override public void setChanged() {
		super.setChanged();
		if (this.level != null && !this.level.isClientSide()) {
			this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		this.wildways$save(output);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.wildways$load(input);
	}

	@Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
	@Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return this.saveWithoutMetadata(registries); }
}
