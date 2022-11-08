package me.aurelium.szworkshop.enchantments;

import me.aurelium.szworkshop.SZWorkshop;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.registry.Registry;

public class SZEnchantments {
	public static Enchantment STOMPING;

	public static void initialize() {
		STOMPING = Registry.register(Registry.ENCHANTMENT, SZWorkshop.id("stomping"), new StompingEnchantment());
	}
}
