package me.aurelium.szworkshop.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.EnumSet;

public class QuiverEntity extends TameableEntity implements IAnimatable, RangedAttackMob, NamedScreenHandlerFactory {
	private static final int ARROW_STAGE_1_COUNT = 1;
	private static final int ARROW_STAGE_2_COUNT = 64;
	private static final int ARROW_STAGE_3_COUNT = 256;

	private final AnimationFactory factory = new AnimationFactory(this);
	private final SimpleInventory inventory = new SimpleInventory(9);
	private static final TrackedData<Integer> ARROWS = DataTracker.registerData(QuiverEntity.class, TrackedDataHandlerRegistry.INTEGER);

	protected QuiverEntity(EntityType<QuiverEntity> entityType, World world) {
		super(entityType, world);
		inventory.addListener(inv -> {
			this.dataTracker.set(ARROWS, countArrows());
		});
	}

	@Override
	protected void mobTick() {
		super.mobTick();
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(1, new SwimGoal(this));
		this.goalSelector.add(2, new ProjectileAttackNoLookGoal(this, 1.25, 2, 20, 10.0F));
		this.goalSelector.add(3, new FollowOwnerNoTeleportGoal(this, 1f, 5f, 2f, false));
		this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));

		this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
		this.targetSelector.add(2, new RevengeGoal(this));
		this.targetSelector.add(3, new AttackWithOwnerGoal(this));
		this.targetSelector
				.add(3, new TargetGoal(this, MobEntity.class, 5, false, false, entity -> entity instanceof Monster && !(entity instanceof CreeperEntity)));
	}

	@Override
	protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
		return 0.85f;
	}

	public static DefaultAttributeContainer.Builder createMobAttributes() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 50f)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2f);
	}

	@Override
	protected void initDataTracker() {
		super.initDataTracker();
		this.dataTracker.startTracking(ARROWS, 0);
	}

	@Override
	protected void onKilledBy(@Nullable LivingEntity adversary) {
		for(int i=0; i < 20; i++) {
			world.addParticle(ParticleTypes.LARGE_SMOKE, getX(), getY(), getZ(), 0f, 0f, 0f);
		}
		// stole this from the ItemScatterer code
		for(int i=0; i < 4; i++) {
			PersistentProjectileEntity arrow = createRandomArrow(1f);

			if(arrow != null) {
				double d = EntityType.ARROW.getWidth();
				double e = 1.0 - d;
				double f = d / 2.0;
				double g = Math.floor(getX()) + world.random.nextDouble() * e + f;
				double h = Math.floor(getY()) + world.random.nextDouble() * e;
				double j = Math.floor(getZ()) + world.random.nextDouble() * e + f;

				arrow.setPos(g, h, j);
				arrow.setVelocity(
						world.random.nextTriangular(0.0, 0.11485000171139836),
						world.random.nextTriangular(0.2, 0.11485000171139836),
						world.random.nextTriangular(0.0, 0.11485000171139836)
				);
				world.spawnEntity(arrow);
			}
		}
		ItemScatterer.spawn(world, this, inventory);
	}

	@Override
	protected void dropInventory() {

	}

	private int countArrows() {
		int count = 0;
		for(ItemStack stack : inventory.stacks) {
			if(stack.getItem() instanceof ArrowItem) {
				count += stack.getCount();
			}
		}
		return count;
	}

	private int chooseSlotWithArrow(RandomGenerator random) {
		int i = -1;
		int j = 1;

		for(int k = 0; k < this.inventory.size(); ++k) {
			if ((this.inventory.getStack(k).getItem() instanceof ArrowItem) && random.nextInt(j++) == 0) {
				i = k;
			}
		}

		return i;
	}

	@Override
	public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
		if(!isTamed())
			setOwner(player);

		if(!player.world.isClient) {
			player.openHandledScreen(this);
		}

		return ActionResult.success(true);
	}


	// Copied from the Donkey, oddly.
	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		NbtList nbtList = new NbtList();

		for(int i = 2; i < this.inventory.size(); ++i) {
			ItemStack itemStack = this.inventory.getStack(i);
			if (!itemStack.isEmpty()) {
				NbtCompound nbtCompound = new NbtCompound();
				nbtCompound.putByte("Slot", (byte)i);
				itemStack.writeNbt(nbtCompound);
				nbtList.add(nbtCompound);
			}
		}

		nbt.put("Inventory", nbtList);
		super.writeCustomDataToNbt(nbt);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		NbtList nbtList = nbt.getList("Inventory", NbtElement.COMPOUND_TYPE);

		for(int i = 0; i < nbtList.size(); ++i) {
			NbtCompound nbtCompound = nbtList.getCompound(i);
			int j = nbtCompound.getByte("Slot") & 255;
			if (j >= 2 && j < this.inventory.size()) {
				this.inventory.setStack(j, ItemStack.fromNbt(nbtCompound));
			}
		}
		super.readCustomDataFromNbt(nbt);
	}

	private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {

		AnimationBuilder builder = new AnimationBuilder();
		builder.addAnimation("animation.quiver.walk", true);

		event.getController().setAnimation(builder);
		return PlayState.CONTINUE;
	}

	private <E extends IAnimatable> PlayState arrowPredicate(AnimationEvent<E> event) {
		int arrows = this.dataTracker.get(ARROWS);

		AnimationBuilder builder = new AnimationBuilder();

		if(arrows >= ARROW_STAGE_3_COUNT) {
			builder.addAnimation("animation.quiver.arrows_3");
		} else if(arrows >= ARROW_STAGE_2_COUNT) {
			builder.addAnimation("animation.quiver.arrows_2");
		} else if(arrows >= ARROW_STAGE_1_COUNT) {
			builder.addAnimation("animation.quiver.arrows_1");
		} else {
			builder.addAnimation("animation.quiver.arrows_0");
		}

		event.getController().setAnimation(builder);
		return PlayState.CONTINUE;
	}

	@Override
	public void registerControllers(AnimationData data) {
		data.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
		data.addAnimationController(new AnimationController<>(this, "arrow", 0, this::arrowPredicate));
	}

	@Override
	public AnimationFactory getFactory() {
		return factory;
	}

	@Nullable
	@Override
	public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
		return new QuiverEntity(SZEntities.QUIVER, world);
	}

	@Nullable
	private PersistentProjectileEntity createRandomArrow(float damageModifier) {
		int randomArrowItem = chooseSlotWithArrow(world.random);

		if(randomArrowItem != -1) {
			ItemStack itemStack = inventory.getStack(randomArrowItem);

			inventory.removeStack(randomArrowItem, 1);

			return ProjectileUtil.createArrowProjectile(this, itemStack, damageModifier);
		}

		return null;
	}

	@Override
	public void attack(LivingEntity target, float pullProgress) {
		PersistentProjectileEntity arrow = createRandomArrow(pullProgress);

		if(arrow != null) {
			double d = target.getX() - this.getX();
			double e = target.getBodyY(0.1f) - arrow.getY();
			double f = target.getZ() - this.getZ();
			double g = Math.sqrt(d * d + f * f);
			arrow.setPos(this.getX() + d * 0.1, this.getY() + 0.9f, this.getZ() + f * 0.1);
			arrow.setVelocity(d, e + g * 0.2F, f, 3F, 0);
			this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
			this.world.spawnEntity(arrow);
		}
	}

	@Nullable
	@Override
	public ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
		return new Generic3x3ContainerScreenHandler(i, playerInventory, inventory);
	}

	// This is just the same as FollowOwnerGoal but with the teleportation code removed.
	private static class FollowOwnerNoTeleportGoal extends Goal {
		private final TameableEntity tameable;
		private LivingEntity owner;
		private final WorldView world;
		private final double speed;
		private final EntityNavigation navigation;
		private int updateCountdownTicks;
		private final float maxDistance;
		private final float minDistance;
		private float oldWaterPathfindingPenalty;
		private final boolean leavesAllowed;

		public FollowOwnerNoTeleportGoal(TameableEntity tameable, double speed, float minDistance, float maxDistance, boolean leavesAllowed) {
			this.tameable = tameable;
			this.world = tameable.world;
			this.speed = speed;
			this.navigation = tameable.getNavigation();
			this.minDistance = minDistance;
			this.maxDistance = maxDistance;
			this.leavesAllowed = leavesAllowed;
			this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
			if (!(tameable.getNavigation() instanceof MobNavigation) && !(tameable.getNavigation() instanceof BirdNavigation)) {
				throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
			}
		}

		@Override
		public boolean canStart() {
			LivingEntity livingEntity = this.tameable.getOwner();
			if (livingEntity == null) {
				return false;
			} else if (livingEntity.isSpectator()) {
				return false;
			} else if (this.tameable.isSitting()) {
				return false;
			} else if (this.tameable.squaredDistanceTo(livingEntity) < (double)(this.minDistance * this.minDistance)) {
				return false;
			} else {
				this.owner = livingEntity;
				return true;
			}
		}

		@Override
		public boolean shouldContinue() {
			if (this.navigation.isIdle()) {
				return false;
			} else if (this.tameable.isSitting()) {
				return false;
			} else {
				return !(this.tameable.squaredDistanceTo(this.owner) <= (double)(this.maxDistance * this.maxDistance));
			}
		}

		@Override
		public void start() {
			this.updateCountdownTicks = 0;
			this.oldWaterPathfindingPenalty = this.tameable.getPathfindingPenalty(PathNodeType.WATER);
			this.tameable.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
		}

		@Override
		public void stop() {
			this.owner = null;
			this.navigation.stop();
			this.tameable.setPathfindingPenalty(PathNodeType.WATER, this.oldWaterPathfindingPenalty);
		}

		@Override
		public void tick() {
			this.tameable.getLookControl().lookAt(this.owner, 10.0F, (float)this.tameable.getLookPitchSpeed());
			if (--this.updateCountdownTicks <= 0) {
				this.updateCountdownTicks = this.getTickCount(10);
				if (!this.tameable.isLeashed() && !this.tameable.hasVehicle()) {
					this.navigation.startMovingTo(this.owner, this.speed);
				}
			}
		}
	}

	// Again, just the vanilla projectile attack goal without making the entity look at the target.
	private static class ProjectileAttackNoLookGoal extends Goal {
		private final MobEntity mob;
		private final RangedAttackMob owner;
		@Nullable
		private LivingEntity target;
		private int updateCountdownTicks = -1;
		private final double mobSpeed;
		private int seenTargetTicks;
		private final int minIntervalTicks;
		private final int maxIntervalTicks;
		private final float maxShootRange;
		private final float squaredMaxShootRange;

		public ProjectileAttackNoLookGoal(RangedAttackMob mob, double mobSpeed, int intervalTicks, float maxShootRange) {
			this(mob, mobSpeed, intervalTicks, intervalTicks, maxShootRange);
		}

		public ProjectileAttackNoLookGoal(RangedAttackMob mob, double mobSpeed, int minIntervalTicks, int maxIntervalTicks, float maxShootRange) {
			if (!(mob instanceof LivingEntity)) {
				throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
			} else {
				this.owner = mob;
				this.mob = (MobEntity)mob;
				this.mobSpeed = mobSpeed;
				this.minIntervalTicks = minIntervalTicks;
				this.maxIntervalTicks = maxIntervalTicks;
				this.maxShootRange = maxShootRange;
				this.squaredMaxShootRange = maxShootRange * maxShootRange;
				this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
			}
		}

		@Override
		public boolean canStart() {
			LivingEntity livingEntity = this.mob.getTarget();
			if (livingEntity != null && livingEntity.isAlive()) {
				this.target = livingEntity;
				return true;
			} else {
				return false;
			}
		}

		@Override
		public boolean shouldContinue() {
			return this.canStart() || this.target.isAlive() && !this.mob.getNavigation().isIdle();
		}

		@Override
		public void stop() {
			this.target = null;
			this.seenTargetTicks = 0;
			this.updateCountdownTicks = -1;
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			double d = this.mob.squaredDistanceTo(this.target.getX(), this.target.getY(), this.target.getZ());
			boolean bl = this.mob.getVisibilityCache().canSee(this.target);
			if (bl) {
				++this.seenTargetTicks;
			} else {
				this.seenTargetTicks = 0;
			}

			if (!(d > (double)this.squaredMaxShootRange) && this.seenTargetTicks >= 5) {
				this.mob.getNavigation().stop();
			} else {
				this.mob.getNavigation().startMovingTo(this.target, this.mobSpeed);
			}

			this.mob.getLookControl().lookAt(this.target, 30.0F, 30.0F);
			if (--this.updateCountdownTicks == 0) {
				if (!bl) {
					return;
				}

				float f = (float)Math.sqrt(d) / this.maxShootRange;
				float g = MathHelper.clamp(f, 0.1F, 1.0F);
				this.owner.attack(this.target, g);
				this.updateCountdownTicks = MathHelper.floor(f * (float)(this.maxIntervalTicks - this.minIntervalTicks) + (float)this.minIntervalTicks);
			} else if (this.updateCountdownTicks < 0) {
				this.updateCountdownTicks = MathHelper.floor(
						MathHelper.lerp(Math.sqrt(d) / (double)this.maxShootRange, (double)this.minIntervalTicks, (double)this.maxIntervalTicks)
				);
			}

		}
	}

}
