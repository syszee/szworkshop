package me.aurelium.szworkshop.mixin;

import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityNavigation.class)
public abstract class EntityNavigationMixin {
	@Shadow
	protected float nodeReachProximity;

	@Shadow
	protected abstract Vec3d getPos();

	@Shadow
	@Final
	protected MobEntity entity;

	@Shadow
	@Nullable
	protected Path currentPath;

	@Shadow
	protected abstract boolean shouldJumpToNextNode(Vec3d currentPos);

	@Shadow
	protected abstract void checkTimeouts(Vec3d currentPos);

	/**
	 * @author Aurelium
	 * @reason Fix MC-94054
	 * Taken from <a href="https://github.com/MinecraftForge/MinecraftForge/blob/0e66b7b21757e510aef706ca320881b9eb268822/patches/minecraft/net/minecraft/world/entity/ai/navigation/PathNavigation.java.patch">this Forge patch.</a>
	 */
	@Overwrite
	public void continueFollowingPath() {
		Vec3d vec3d = this.getPos();
		this.nodeReachProximity = this.entity.getWidth() > 0.75F ? this.entity.getWidth() / 2.0F : 0.75F - this.entity.getWidth() / 2.0F;
		Vec3i vec3i = this.currentPath.getCurrentNodePos();
		//double d = Math.abs(this.entity.getX() - ((double)vec3i.getX() + 0.5));
		double d = Math.abs(this.entity.getX() - ((double)vec3i.getX() + (this.entity.getWidth() + 1) / 2D));
		double e = Math.abs(this.entity.getY() - (double)vec3i.getY());
		//double f = Math.abs(this.entity.getZ() - ((double)vec3i.getZ() + 0.5));
		double f = Math.abs(this.entity.getZ() - ((double)vec3i.getZ() + (this.entity.getWidth() + 1) / 2D));
		//boolean bl = d < (double)this.nodeReachProximity && f < (double)this.nodeReachProximity && e < 1.0;
		boolean bl = d <= (double)this.nodeReachProximity && f < (double)this.nodeReachProximity && e < 1.0;
		if (bl || this.entity.canJumpToNextPathNode(this.currentPath.getCurrentNode().type) && this.shouldJumpToNextNode(vec3d)) {
			this.currentPath.next();
		}

		this.checkTimeouts(vec3d);
	}
}
