package me.aurelium.szworkshop.mixin;

import me.aurelium.szworkshop.SZWorkshop;
import me.aurelium.szworkshop.enchantments.SZEnchantments;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.tag.ItemTags;
import net.minecraft.world.World;
import org.apache.commons.compress.harmony.pack200.NewAttributeBands;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin extends ProjectileEntity {
	@Mutable
	@Shadow
	@Final
	private int lureLevel;

	@Shadow
	private int waitCountdown;

	@Shadow
	@Nullable
	public abstract PlayerEntity getPlayerOwner();

	private boolean isEnhanced = false;

	public FishingBobberEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;II)V")
	private void addGlowSquidBonus(CallbackInfo ci) {
		if(!world.isClient) {
			if(((ServerWorld)world).getServer().getGameRules().getBoolean(SZWorkshop.glowsquidFishingRule)) {
				int entityCount = world.getNonSpectatingEntities(GlowSquidEntity.class, this.getBoundingBox().expand(10)).size();
				if (entityCount > 0) {
					isEnhanced = true;
				}
				lureLevel += entityCount;
			}
		}
	}

	@Inject(at = @At("TAIL"), method = "tickFishingLogic")
	public void addParticlesToFishingLogic(CallbackInfo ci) {
		if(!world.isClient && isEnhanced) {
			((ServerWorld)world).spawnParticles(ParticleTypes.GLOW, getX(), getY(), getZ(), 1, random.nextFloat() * 0.2, (random.nextFloat() + 0.5) * 0.2, random.nextFloat() * 0.2, 0.5);
		}

		if(this.waitCountdown < 0)
			this.waitCountdown = 50;
	}

	/** ENCHANTMENT: ORB ANGLER **/
	@ModifyArg(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ExperienceOrbEntity;<init>(Lnet/minecraft/world/World;DDDI)V"), index = 4)
	private int modifyExperienceOrbValue(int value) {
		int OrbAnglerLevel = EnchantmentHelper.getEquipmentLevel(SZEnchantments.ORB_ANGLER, this.getPlayerOwner());
		if(OrbAnglerLevel > 0 && world.getGameRules().getBoolean(SZWorkshop.orbanglerEnchantmentRule))
				return this.random.nextBetween(2, 8)+1;
		else return this.random.nextInt(6)+1;
	}

	/** ENCHANTMENT: MULTICATCH **/
	@Inject(method = "use", at = @At(value = "INVOKE", target ="Lnet/minecraft/entity/ExperienceOrbEntity;<init>(Lnet/minecraft/world/World;DDDI)V"))
	private void multiCatch(ItemStack usedItem, CallbackInfoReturnable<Integer> cir){
		int multicatchLevel = EnchantmentHelper.getEquipmentLevel(SZEnchantments.MULTICATCH, this.getPlayerOwner());
		int chance = random.nextInt(7);
		if(multicatchLevel > 0 && chance == 1 && world.getGameRules().getBoolean(SZWorkshop.multicatchEnchantmentRule)){
			int luckOfTheSeaLevel = EnchantmentHelper.getEquipmentLevel(Enchantments.LUCK_OF_THE_SEA, this.getPlayerOwner());

			LootContext.Builder builder = (new LootContext.Builder((ServerWorld)this.world)).parameter(LootContextParameters.ORIGIN,
							this.getPos()).parameter(LootContextParameters.TOOL, usedItem).parameter(LootContextParameters.THIS_ENTITY, this)
					.random(this.random).luck((float)luckOfTheSeaLevel + getPlayerOwner().getLuck());

			LootTable lootTable = this.world.getServer().getLootManager().getTable(LootTables.FISHING_GAMEPLAY);
			List<ItemStack> list = lootTable.generateLoot(builder.build(LootContextTypes.FISHING));
			Iterator var7 = list.iterator();

			while(var7.hasNext()) {
				ItemStack itemStack = (ItemStack)var7.next();
				ItemEntity itemEntity = new ItemEntity(this.world, this.getX(), this.getY(), this.getZ(), itemStack);
				double d = getPlayerOwner().getX() - this.getX();
				double e = getPlayerOwner().getY() - this.getY();
				double f = getPlayerOwner().getZ() - this.getZ();
				double g = 0.1;
				itemEntity.setVelocity(d * 0.1, e * 0.1 + Math.sqrt(Math.sqrt(d * d + e * e + f * f)) * 0.08, f * 0.1);
				this.world.spawnEntity(itemEntity);
				if (itemStack.isIn(ItemTags.FISHES)) {
					getPlayerOwner().increaseStat(Stats.FISH_CAUGHT, 1);
				}
			}
		}

	}

}
