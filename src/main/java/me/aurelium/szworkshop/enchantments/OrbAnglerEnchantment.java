package me.aurelium.szworkshop.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

public class OrbAnglerEnchantment extends Enchantment {

	/**
	 * Gives the user more experience from fishing.
	 */

	public OrbAnglerEnchantment() {
		super(Rarity.RARE, EnchantmentTarget.FISHING_ROD, EquipmentSlot.values());
	}


	@Override
	public int getMinPower(int level) {
		return level * 25;
	}

	@Override
	public int getMaxPower(int level) {
		return this.getMinPower(level) + 50;
	}

	@Override
	public boolean isTreasure() {
		return true;
	}

	@Override
	public boolean isAvailableForEnchantedBookOffer() {
		return false;
	}

	@Override
	public boolean isAvailableForRandomSelection() {
		return false;
	}

	@Override
	public int getMaxLevel() {
		return 1;
	}
}
