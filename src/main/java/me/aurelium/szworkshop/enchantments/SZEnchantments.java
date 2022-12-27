package me.aurelium.szworkshop.enchantments;

import me.aurelium.szworkshop.SZWorkshop;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.registry.Registry;

public class SZEnchantments {
	public static Enchantment STOMPING, ORB_ANGLER, MULTICATCH;

	public static void initialize() {
		STOMPING = Registry.register(Registry.ENCHANTMENT, SZWorkshop.id("stomping"), new StompingEnchantment());
		ORB_ANGLER = Registry.register(Registry.ENCHANTMENT, SZWorkshop.id("orb_angler"), new OrbAnglerEnchantment());
		MULTICATCH = Registry.register(Registry.ENCHANTMENT, SZWorkshop.id("multicatch"), new OrbAnglerEnchantment());
	}
}
