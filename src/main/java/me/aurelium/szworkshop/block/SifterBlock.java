package me.aurelium.szworkshop.block;

import me.aurelium.szworkshop.SZWorkshop;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SifterBlock extends Block implements InventoryProvider {
	public static final IntProperty GRAVEL_AMOUNT = IntProperty.of("gravel", 0, 4);

	private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);

	public SifterBlock() {
		super(FabricBlockSettings.of(Material.WOOD).nonOpaque().strength(2f).sounds(BlockSoundGroup.WOOD));
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if(world.isClient)
			return ActionResult.SUCCESS;

		if(!((ServerWorld)world).getServer().getGameRules().getBoolean(SZWorkshop.sifterRule)) {
			player.sendMessage(Text.translatable("block.szworkshop.sifter.disabled"), true);
			return ActionResult.SUCCESS;
		}

		int gravelAmount = state.get(GRAVEL_AMOUNT);

		if(gravelAmount == 0) {
			ItemStack stackInHand = player.getStackInHand(hand);

			if (stackInHand.isItemEqual(new ItemStack(Blocks.GRAVEL))) {
				stackInHand.decrement(1);
				world.setBlockState(pos, state.with(GRAVEL_AMOUNT, 4));
			}
		}

		sift(world, pos, state);

		return ActionResult.SUCCESS;
	}

	public void sift(World world, BlockPos pos, BlockState state) {
		int gravelAmount = state.get(GRAVEL_AMOUNT);

		if(gravelAmount == 0) {


		} else {
			world.setBlockState(pos, state.with(GRAVEL_AMOUNT, gravelAmount-1));
			if(gravelAmount == 1) {
				LootTable table = world.getServer().getLootManager().getTable(SZWorkshop.id("sifter"));

				// this is just the easiest and most applicable loot table type for this - making a custom one would be very annoying
				List<ItemStack> stacks = table.generateLoot(
					new LootContext.Builder((ServerWorld) world)
						.parameter(LootContextParameters.ORIGIN, new Vec3d(pos.getX(), pos.getY(), pos.getZ()))
						.parameter(LootContextParameters.BLOCK_STATE, state)
						.parameter(LootContextParameters.TOOL, ItemStack.EMPTY)
						.build(LootContextTypes.BLOCK)
				);

				DefaultedList<ItemStack> defaultedStacks = DefaultedList.copyOf(ItemStack.EMPTY, stacks.toArray(new ItemStack[0]));

				ItemScatterer.spawn(world, pos.up(), defaultedStacks);
			}
			world.playSound(null, pos, SoundEvents.BLOCK_GRAVEL_PLACE, SoundCategory.BLOCKS, 1.0f, 1.0f);
		}


	}

	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
		if(!world.isClient && world.getReceivedRedstonePower(pos) > 0) {
			sift(world, pos, state);
		}
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(GRAVEL_AMOUNT);
	}

	@Override
	public SidedInventory getInventory(BlockState state, WorldAccess world, BlockPos pos) {
		int amount = state.get(GRAVEL_AMOUNT);
		if(amount == 0) {
			return new InsertableInventory(state, world, pos);
		}

		return new DummyInventory();
	}

	private static class InsertableInventory extends SimpleInventory implements SidedInventory {
		private final BlockState state;
		private final WorldAccess world;
		private final BlockPos pos;
		private boolean dirty;

		public InsertableInventory(BlockState state, WorldAccess world, BlockPos pos) {
			super(1);
			this.state = state;
			this.world = world;
			this.pos = pos;
		}

		@Override
		public int getMaxCountPerStack() {
			return 1;
		}

		@Override
		public int[] getAvailableSlots(Direction side) {
			return new int[1];
		}

		@Override
		public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
			return !this.dirty && dir == Direction.UP && stack.isItemEqual(new ItemStack(Blocks.GRAVEL));
		}

		@Override
		public boolean canExtract(int slot, ItemStack stack, Direction dir) {
			return false;
		}

		@Override
		public void markDirty() {
			ItemStack itemStack = this.getStack(0);
			if (!itemStack.isEmpty()) {
				this.dirty = true;
				world.setBlockState(pos, SZBlocks.SIFTER.getDefaultState().with(SifterBlock.GRAVEL_AMOUNT, 4), Block.NOTIFY_ALL);
				this.removeStack(0);
			}
		}
	}

	private static class DummyInventory extends SimpleInventory  implements SidedInventory {
		public DummyInventory() {
			super(0);
		}

		@Override
		public int[] getAvailableSlots(Direction side) {
			return new int[0];
		}

		@Override
		public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
			return false;
		}

		@Override
		public boolean canExtract(int slot, ItemStack stack, Direction dir) {
			return false;
		}
	}
}
