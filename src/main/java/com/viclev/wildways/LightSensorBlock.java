package com.viclev.wildways;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;

public class LightSensorBlock extends Block {
	public static final IntegerProperty POWER = BlockStateProperties.POWER;
	private static final int UPDATE_INTERVAL_TICKS = 20;

	public LightSensorBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(POWER, 0));
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return simpleCodec(LightSensorBlock::new);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(POWER);
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
		return state.getValue(POWER);
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		if (!level.isClientSide() && !state.is(oldState.getBlock())) {
			this.updatePower(state, level, pos);
			level.scheduleTick(pos, this, UPDATE_INTERVAL_TICKS);
		}
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		this.updatePower(state, level, pos);
		level.scheduleTick(pos, this, UPDATE_INTERVAL_TICKS);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block changedBlock, Orientation orientation, boolean movedByPiston) {
		if (!level.isClientSide()) {
			this.updatePower(state, level, pos);
		}
	}

	private void updatePower(BlockState state, Level level, BlockPos pos) {
		int power = level.getMaxLocalRawBrightness(pos.above());
		if (state.getValue(POWER) != power) {
			level.setBlock(pos, state.setValue(POWER, power), Block.UPDATE_ALL);
			level.updateNeighbourForOutputSignal(pos, this);
		}
	}
}
