package me.aurelium.szworkshop.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SifterBlock extends Block {
	public static final IntProperty GRAVEL_AMOUNT = IntProperty.of("gravel", 0, 4);

	public SifterBlock() {
		super(FabricBlockSettings.of(Material.WOOD).nonOpaque().strength(2f));
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		int gravelAmount = state.get(GRAVEL_AMOUNT);

		if(gravelAmount == 0) {
			ItemStack stackInHand = player.getStackInHand(hand);

			if(stackInHand.isItemEqual(new ItemStack(Blocks.GRAVEL))) {
				stackInHand.decrement(1);
				return ActionResult.success(world.isClient);
			}
		} else {
			world.setBlockState(pos, state.with(GRAVEL_AMOUNT, gravelAmount-1));
			return ActionResult.success(world.isClient);
		}

		return super.onUse(state, world, pos, player, hand, hit);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(GRAVEL_AMOUNT);
	}
}
