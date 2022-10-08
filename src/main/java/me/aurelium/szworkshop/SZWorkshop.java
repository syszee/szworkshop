package me.aurelium.szworkshop;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SZWorkshop implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod name as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("SystemZee's Workshop");

	public static String modid;

	// Game rules, if there ends up being like 20 of these it'd be best to separate into a different file.
	public static GameRules.Key<GameRules.BooleanRule> quiverRule;

	@Override
	public void onInitialize(ModContainer mod) {
		modid = mod.metadata().id();

		quiverRule = GameRuleRegistry.register("szwQuiver", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(true));
	}
}
