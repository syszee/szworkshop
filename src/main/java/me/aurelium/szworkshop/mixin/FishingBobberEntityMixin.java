package me.aurelium.szworkshop.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin extends ProjectileEntity {
	@Mutable
	@Shadow
	@Final
	private int lureLevel;

	private boolean isEnhanced = false;

	public FishingBobberEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;II)V")
	private void addGlowSquidBonus(CallbackInfo ci) {
		int entityCount = world.getNonSpectatingEntities(GlowSquidEntity.class, this.getBoundingBox().expand(10)).size()*2;
		if(entityCount > 0) {
			isEnhanced = true;
		}
		lureLevel += entityCount;
	}

	@Inject(at = @At("TAIL"), method = "tickFishingLogic")
	public void addParticlesToFishingLogic(CallbackInfo ci) {
		if(!world.isClient && isEnhanced) {
			((ServerWorld)world).spawnParticles(ParticleTypes.GLOW, getX(), getY(), getZ(), 1, random.nextFloat() * 0.2, (random.nextFloat() + 0.5) * 0.2, random.nextFloat() * 0.2, 0.5);
		}
	}
}
