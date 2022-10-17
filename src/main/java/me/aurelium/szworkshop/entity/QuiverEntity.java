package me.aurelium.szworkshop.entity;

import me.aurelium.szworkshop.SZWorkshop;
import me.aurelium.szworkshop.sound.SZSoundEvents;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.EnumSet;
import java.util.random.RandomGenerator;

public class QuiverEntity extends TameableEntity implements IAnimatable, RangedAttackMob, NamedScreenHandlerFactory {
	private static final int ARROW_STAGE_1_COUNT = 1;
	private static final int ARROW_STAGE_2_COUNT = 64;
	private static final int ARROW_STAGE_3_COUNT = 256;

	private final AnimationFactory factory = new AnimationFactory(this);
	private final SimpleInventory inventory = new SimpleInventory(9);
	private static final TrackedData<Integer> ARROWS = DataTracker.registerData(QuiverEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Boolean> HONEYED = DataTracker.registerData(QuiverEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

	public QuiverEntity(EntityType<QuiverEntity> entityType, World world) {
		super(entityType, world);
		inventory.addListener(inv -> this.dataTracker.set(ARROWS, countArrows()));
	}

	// sound-related events

	@Nullable
	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SZSoundEvents.QUIVER_DAMAGE;
	}

	@Nullable
	@Override
	protected SoundEvent getDeathSound() {
		return SZSoundEvents.QUIVER_DEATH;
	}

	// Behavior methods

	@Override
	protected void mobTick() {
		if(!world.getServer().getGameRules().getBoolean(SZWorkshop.quiverRule)) {
			this.kill();
		}
		super.mobTick();
	}

	@Override
	public void tickMovement() {

		super.tickMovement();
	}

	@Override
	public void onDeath(DamageSource source) {
		for(int i = 0; i < 40; ++i) {
			this.world.addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, this.getParticleX(0.5), this.getRandomBodyY(), this.getParticleZ(0.5), 0.01-(getRandom().nextDouble()*0.02), 0.1+getRandom().nextDouble()*0.1, 0.01-(getRandom().nextDouble()*0.02));
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
						world.random.nextTriangular(0.0, 0.11485000171139836), // wish I knew what caused the decompiler to generate these weird constants
						world.random.nextTriangular(0.2, 0.11485000171139836), // but I'm not touching them
						world.random.nextTriangular(0.0, 0.11485000171139836)
				);
				world.spawnEntity(arrow);
			}
		}

		// Inventory pops out like when you break a chest
		ItemScatterer.spawn(world, this, inventory);

		super.onDeath(source);
	}

	@Override
	public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
		// You can't actually know what player built a golem in the vanilla system.
		// Rather than something stupid like mixing into the button press that places blocks, it just bonds with whoever interacts with it first.
		if(!isTamed())
			setOwner(player);

		if(player.isSneaking())
			return super.interactAt(player, hitPos, hand);

		ItemStack stackInHand = player.getStackInHand(hand);

		if(!isHoneyed() && stackInHand.getItem() == Items.HONEY_BOTTLE) {
			if (!world.isClient) {
				ServerWorld serverWorld = (ServerWorld)world;
				for (int i = 0; i < 50; ++i) {
					serverWorld.spawnParticles(ParticleTypes.LANDING_HONEY, this.getX() + (0.5-world.random.nextDouble()), this.getY()+1, this.getZ() + (0.5-world.random.nextDouble()), 1, 0.0, -1, 0.0, 1.0);
				}
			}
			world.playSound(null, this.getBlockPos(), SoundEvents.BLOCK_HONEY_BLOCK_BREAK, SoundCategory.BLOCKS, 1.0f, 1.0f);

			player.setStackInHand(hand, ItemUsage.exchangeStack(stackInHand, player, new ItemStack(Items.GLASS_BOTTLE)));
			setHoneyed(true);
		} else if(isHoneyed() && stackInHand.getItem() == Items.WATER_BUCKET) {
			setHoneyed(false);
			world.playSound(null, this.getBlockPos(), SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
		} else if(isHoneyed() && stackInHand.getItem() == Items.POTION) {
			if(PotionUtil.getPotion(stackInHand) == Potions.WATER) { // de-honey, most of this is taken from the "turn block into mud" code in PotionItem
				if (!world.isClient) {
					ServerWorld serverWorld = (ServerWorld)world;
					for (int i = 0; i < 5; ++i) {
						serverWorld.spawnParticles(ParticleTypes.SPLASH, this.getX() + world.random.nextDouble(), this.getY() + 1, this.getZ() + world.random.nextDouble(), 1, 0.0, 0.0, 0.0, 1.0);
					}
				}
				world.playSound(null, this.getBlockPos(), SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);

				player.setStackInHand(hand, ItemUsage.exchangeStack(stackInHand, player, new ItemStack(Items.GLASS_BOTTLE)));

				setHoneyed(false);
			}
		} else {
			if (!player.world.isClient) {
				player.openHandledScreen(this);
			}
		}

		return ActionResult.success(true);
	}

	@Override
	public void attack(LivingEntity target, float pullProgress) {
		PersistentProjectileEntity arrow = createRandomArrow(pullProgress);

		if(arrow != null) {
			double d = target.getX() - this.getX();
			double e = target.getBodyY(1/32f) - arrow.getY();
			double f = target.getZ() - this.getZ();
			double g = Math.sqrt(d * d + f * f);
			//arrow.setPos(this.getX() + d * 0.1, this.getY() + 0.9f, this.getZ() + f * 0.1);
			arrow.setVelocity(d, e + g * 0.2F, f, 1.6F, 0);
			this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
			this.world.spawnEntity(arrow);
		}
	}

	@Override
	protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
		return 0.85f;
	}

	// Data-tracking and AI methods

	@Override
	protected void initGoals() {
		this.goalSelector.add(1, new SwimGoal(this));
		this.goalSelector.add(2, new QuiverProjectileAttackGoal(this, 1.25, 0, 20, 10.0F));
		this.goalSelector.add(5, new FollowOwnerNoTeleportGoal(this, 1f, 10f, 2f));
		this.goalSelector.add(8, new WanderAroundFarGoal(this, 1.0));
		this.goalSelector.add(10, new LookAroundGoal(this));
		//this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));

		this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
		this.targetSelector.add(2, new RevengeGoal(this));
		this.targetSelector.add(3, new AttackWithOwnerGoal(this));
		this.targetSelector.add(4, new ActiveTargetGoal<>(this, MobEntity.class, 5, false, false, entity -> entity instanceof Monster && !(entity instanceof CreeperEntity)));
	}

	public static DefaultAttributeContainer.Builder createMobAttributes() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 12f)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3f);
	}

	@Override
	protected void initDataTracker() {
		super.initDataTracker();
		this.dataTracker.startTracking(ARROWS, 0);
		this.dataTracker.startTracking(HONEYED, false);
	}

	@Override
	public void takeKnockback(double strength, double x, double z) { // I want it to appear stuck in place when honeyed.
		if(!isHoneyed()) {
			super.takeKnockback(strength, x, z);
		}
	}

	public boolean isHoneyed() {
		return this.dataTracker.get(HONEYED);
	}

	private void setHoneyed(boolean honeyed) {
		if(honeyed) {
			this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0f);
		} else {
			this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.3f);
		}
		this.dataTracker.set(HONEYED, honeyed);
	}

	// Inventory-related methods

	private int countArrows() {
		int count = 0;
		for(ItemStack stack : inventory.stacks) {
			if(stack.getItem() instanceof ArrowItem) {
				count += stack.getCount();
			}
		}
		return count;
	}

	private int chooseSlotWithArrow(Random random) {
		int i = -1;
		int j = 1;

		for(int k = 0; k < this.inventory.size(); ++k) {
			if ((this.inventory.getStack(k).getItem() instanceof ArrowItem) && random.nextInt(j++) == 0) {
				i = k;
			}
		}

		return i;
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

	@Nullable
	@Override
	public ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
		return new Generic3x3ContainerScreenHandler(i, playerInventory, inventory);
	}

	// NBT Serialization methods
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
		nbt.putBoolean("Honeyed", isHoneyed());

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

		setHoneyed(nbt.getBoolean("Honeyed"));
		super.readCustomDataFromNbt(nbt);
	}



	// Animation methods

	private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {

		AnimationBuilder builder = new AnimationBuilder();
		if(event.isMoving() && !isHoneyed()) {
			builder.addAnimation("animation.quiver.walk_alt", true);
		} else {
			builder.addAnimation("animation.quiver.idle_alt", true);
		}

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

	// Unfortunately we need this to take advantage of tameable mob code.
	@Nullable
	@Override
	public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
		return new QuiverEntity(SZEntities.QUIVER, world);
	}

	// This is just the same as FollowOwnerGoal but with the teleportation code removed.
	private static class FollowOwnerNoTeleportGoal extends Goal {
		private final TameableEntity tameable;
		private LivingEntity owner;
		private final double speed;
		private final EntityNavigation navigation;
		private int updateCountdownTicks;
		private final float maxDistance;
		private final float minDistance;
		private float oldWaterPathfindingPenalty;

		public FollowOwnerNoTeleportGoal(TameableEntity tameable, double speed, float minDistance, float maxDistance) {
			this.tameable = tameable;
			this.speed = speed;
			this.navigation = tameable.getNavigation();
			this.minDistance = minDistance;
			this.maxDistance = maxDistance;
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
			this.tameable.getLookControl().lookAt(this.owner, 10.0F, (float)this.tameable.getMaxLookPitchChange());
			if (--this.updateCountdownTicks <= 0) {
				this.updateCountdownTicks = this.getTickCount(10);
				if (!this.tameable.isLeashed() && !this.tameable.hasVehicle()) {
					this.navigation.startMovingTo(this.owner, this.speed);
				}
			}
		}
	}

	// Again, just the vanilla projectile attack goal without making the entity look at the target.
	private static class QuiverProjectileAttackGoal extends Goal {
		private final QuiverEntity mob;
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

		public QuiverProjectileAttackGoal(QuiverEntity mob, double mobSpeed, int minIntervalTicks, int maxIntervalTicks, float maxShootRange) {
			if (mob == null) {
				throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
			} else {
				this.owner = mob;
				this.mob = mob;
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
			if(this.mob.dataTracker.get(QuiverEntity.ARROWS) == 0) { // stops it from staring at random mobs when it has no arrows
				return false;
			}
			if (livingEntity != null && livingEntity.isAlive()) {
				this.target = livingEntity;
				return !(this.target.hurtTime > 0);
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
		public boolean shouldRunEveryTick() {
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

			this.mob.getLookControl().lookAt(this.target);
			this.mob.lookAtEntity(this.target, 30f, 30f);
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
						MathHelper.lerp(Math.sqrt(d) / (double)this.maxShootRange, this.minIntervalTicks, this.maxIntervalTicks)
				);
			}

		}
	}

}
