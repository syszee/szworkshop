package me.aurelium.szworkshop;

import me.aurelium.szworkshop.entity.SZEntities;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
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
	public static GameRules.Key<GameRules.BooleanRule> understandRule;
	public static GameRules.Key<GameRules.BooleanRule> quiverRule;

	private static void verifyWorldChanges(MinecraftServer server, GameRules.BooleanRule rule) {
		if(!rule.get()) {
			if(!server.getGameRules().getBoolean(understandRule)) {
				for(PlayerEntity player : server.getPlayerManager().getPlayerList()) {
					player.sendMessage(Text.translatable("szworkshop.verifyWorldChanges"), false);
				}
				rule.set(true, server);
			}
		}
	}

	@Override
	public void onInitialize(ModContainer mod) {
		modid = mod.metadata().id();

		understandRule = GameRuleRegistry.register("szwUnderstand", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));

		quiverRule = GameRuleRegistry.register("szwQuiver", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(true, SZWorkshop::verifyWorldChanges));

		SZEntities.initialize();
	}
}
