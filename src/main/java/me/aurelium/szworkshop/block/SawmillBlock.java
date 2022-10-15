package me.aurelium.szworkshop.block;

import me.aurelium.szworkshop.SZWorkshop;
import me.aurelium.szworkshop.ui.screen.SawmillScreenHandler;
import net.minecraft.block.*;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SawmillBlock extends Block {
	private static final Text TITLE = Text.translatable("container.szworkshop.saw_mill");
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 10.0, 16.0);

	public SawmillBlock() {
		super(Settings.of(Material.WOOD).sounds(BlockSoundGroup.WOOD).strength(1f).nonOpaque());
	}

	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
	}

	@Override
	public boolean hasSidedTransparency(BlockState state) {
		return true;
	}

	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (world.isClient) {
			return ActionResult.SUCCESS;
		} else {
			if(world.getServer().getGameRules().getBoolean(SZWorkshop.sawmillRule)) {
				player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
				player.incrementStat(Stats.INTERACT_WITH_STONECUTTER);
			} else {
				player.sendMessage(Text.translatable("block.szworkshop.saw_mill.disabled"), true);
			}
			return ActionResult.CONSUME;
		}
	}

	@Nullable
	public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
		return new SimpleNamedScreenHandlerFactory((syncId, playerInventory, player) -> {
			return new SawmillScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(world, pos));
		}, TITLE);
	}

	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	public boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos) {
		return false;
	}

	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}



	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	public BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}

	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
		return false;
	}
}
