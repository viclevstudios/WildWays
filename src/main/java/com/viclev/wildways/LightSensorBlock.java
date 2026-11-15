package com.viclev.wildways;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;

public class LightSensorBlock extends Block {
	public static final IntegerProperty POWER = BlockStateProperties.POWER;
	private static final int UPDATE_INTERVAL_TICKS = 20;
	private static final Identifier SOLAR_POWERED = Wildways.id("adventure/solar_powered");
	private static final double ADVANCEMENT_RANGE_SQUARED = 8 * 8;

	public LightSensorBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(POWER, 0));
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
		if (this.updatePower(state, level, pos) > 0 && !level.players().isEmpty()) {
			this.checkComparatorOutput(level, pos);
		}
		level.scheduleTick(pos, this, UPDATE_INTERVAL_TICKS);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block changedBlock, Orientation orientation, boolean movedByPiston) {
		if (!level.isClientSide()) {
			this.updatePower(state, level, pos);
		}
	}

	private int updatePower(BlockState state, Level level, BlockPos pos) {
		int power = level.getMaxLocalRawBrightness(pos.above());
		if (state.getValue(POWER) != power) {
			level.setBlock(pos, state.setValue(POWER, power), Block.UPDATE_ALL);
			level.updateNeighbourForOutputSignal(pos, this);
		}
		return power;
	}

	private void checkComparatorOutput(ServerLevel level, BlockPos pos) {
		AdvancementHolder advancement = level.getServer().getAdvancements().get(SOLAR_POWERED);
		if (advancement == null) {
			return;
		}
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			BlockPos comparatorPos = pos.relative(direction);
			if (!level.hasChunkAt(comparatorPos)) {
				continue;
			}
			BlockState comparator = level.getBlockState(comparatorPos);
			if (comparator.is(Blocks.COMPARATOR) && comparator.getSignal(level, comparatorPos, direction.getOpposite()) > 0) {
				for (ServerPlayer player : level.players()) {
					if (!player.isSpectator() && pos.distToCenterSqr(player.position()) <= ADVANCEMENT_RANGE_SQUARED) {
						player.getAdvancements().award(advancement, "complete");
					}
				}
				return;
			}
		}
	}
}
