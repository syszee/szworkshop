package me.aurelium.szworkshop.mixin;

import me.aurelium.szworkshop.item.SZItems;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlowSquidEntity.class)
public class GlowSquidEntityMixin extends SquidEntity implements Bucketable {
	public GlowSquidEntityMixin(EntityType<? extends SquidEntity> entityType, World world) {
		super(entityType, world);
	}

	@Override
	public boolean isFromBucket() {
		return false;
	}

	@Override
	public void setFromBucket(boolean fromBucket) {

	}

	@Override
	public void copyDataToStack(ItemStack stack) {
		Bucketable.copyDataToStack((GlowSquidEntity)(Object)this, stack);
	}

	@Override
	public void copyDataFromNbt(NbtCompound nbt) {
		Bucketable.copyDataFromNbt((GlowSquidEntity)(Object)this, nbt);
	}

	@Override
	public ItemStack getBucketItem() {
		return new ItemStack(SZItems.GLOW_SQUID_BUCKET);
	}

	@Override
	public SoundEvent getBucketFillSound() {
		return SoundEvents.ITEM_BUCKET_FILL_FISH;
	}

	@Override
	protected ActionResult interactMob(PlayerEntity player, Hand hand) {
		return Bucketable.tryBucket(player, hand, this).orElse(super.interactMob(player, hand));
	}
}
