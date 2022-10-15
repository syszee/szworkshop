package me.aurelium.szworkshop.mixin;

import me.aurelium.szworkshop.SZWorkshop;
import me.aurelium.szworkshop.entity.QuiverEntity;
import me.aurelium.szworkshop.entity.SZEntities;
import me.aurelium.szworkshop.sound.SZSoundEvents;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(CarvedPumpkinBlock.class)
public class CarvedPumpkinMixin {
	@Shadow
	@Final
	private static Predicate<BlockState> IS_GOLEM_HEAD_PREDICATE;
	private BlockPattern quiverPattern;

	private BlockPattern getQuiverPattern() {
		if (this.quiverPattern == null) {
			this.quiverPattern = BlockPatternBuilder.start()
					.aisle("^", "D", "C")
					.where('^', CachedBlockPosition.matchesBlockState(IS_GOLEM_HEAD_PREDICATE))
					.where('D', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.DISPENSER)))
					.where('C', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.COMPOSTER)))
					.build();
		}

		return this.quiverPattern;
	}

	@Inject(method = "trySpawnEntity(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", at = @At("TAIL"))
	public void trySpawnQuiver(World world, BlockPos pos, CallbackInfo ci) {
		BlockPattern.Result result = this.getQuiverPattern().searchAround(world, pos);

		if (result != null && world.getServer().getGameRules().getBoolean(SZWorkshop.quiverRule)) {
			for(int i = 0; i < this.getQuiverPattern().getHeight(); ++i) {
				CachedBlockPosition cachedBlockPosition = result.translate(0, i, 0);
				world.setBlockState(cachedBlockPosition.getBlockPos(), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
				world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, cachedBlockPosition.getBlockPos(), Block.getRawIdFromState(cachedBlockPosition.getBlockState()));
			}

			QuiverEntity quiverEntity = SZEntities.QUIVER.create(world);
			BlockPos blockPos = result.translate(0, 2, 0).getBlockPos();
			quiverEntity.refreshPositionAndAngles((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.05, (double)blockPos.getZ() + 0.5, 0.0F, 0.0F);
			world.spawnEntity(quiverEntity);

			for(ServerPlayerEntity serverPlayerEntity : world.getNonSpectatingEntities(ServerPlayerEntity.class, quiverEntity.getBoundingBox().expand(5.0))) {
				Criteria.SUMMONED_ENTITY.trigger(serverPlayerEntity, quiverEntity);
			}

			for(int j = 0; j < this.getQuiverPattern().getHeight(); ++j) {
				CachedBlockPosition cachedBlockPosition2 = result.translate(0, j, 0);
				world.updateNeighbors(cachedBlockPosition2.getBlockPos(), Blocks.AIR);
			}

			world.playSound(null, blockPos, SZSoundEvents.QUIVER_CREATE, SoundCategory.BLOCKS, 1.0f, 1.0f);
		}
	}
}
