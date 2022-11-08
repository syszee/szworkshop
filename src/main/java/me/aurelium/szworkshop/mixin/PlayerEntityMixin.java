package me.aurelium.szworkshop.mixin;

import me.aurelium.szworkshop.SZWorkshop;
import me.aurelium.szworkshop.enchantments.SZEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(at = @At("HEAD"), method= "handleFallDamage(FFLnet/minecraft/entity/damage/DamageSource;)Z", cancellable = true)
	public void stompingFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> ci) {
		int stompingLevel = EnchantmentHelper.getEquipmentLevel(SZEnchantments.STOMPING, this);
		if (stompingLevel > 0 && world.getGameRules().getBoolean(SZWorkshop.stompingEnchantmentRule)) {
			DamageSource damageSource2 = DamageSource.ANVIL;
			Predicate<Entity> predicate = EntityPredicates.VALID_LIVING_ENTITY;

			int i = MathHelper.ceil(fallDistance - 1.0f);
			if (i >= 4) {

				float f = Math.min(MathHelper.floor((float) i * 2) * stompingLevel, 10 * stompingLevel);
				List<Entity> otherEntities = this.world.getOtherEntities(this, this.getBoundingBox().expand(1.1), predicate);
				if (otherEntities.size() > 0) {
					otherEntities.forEach(entity -> entity.damage(damageSource2, f));
					ci.setReturnValue(false);
				}
			}

		}
	}
}
