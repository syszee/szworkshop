package me.aurelium.szworkshop;

import me.aurelium.szworkshop.block.SZBlocks;
import me.aurelium.szworkshop.entity.SZEntities;
import me.aurelium.szworkshop.item.SZItems;
import me.aurelium.szworkshop.recipe.SZRecipes;
import me.aurelium.szworkshop.sound.SZSoundEvents;
import me.aurelium.szworkshop.ui.screen.SZScreenHandlers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SZWorkshop implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("SystemZee's Workshop");

	public static final String MODID = "szworkshop";

	// Game rules, if there ends up being like 20 of these it'd be best to separate into a different file.
	public static GameRules.Key<GameRules.BooleanRule> understandRule;
	public static GameRules.Key<GameRules.BooleanRule> quiverRule;
	public static GameRules.Key<GameRules.BooleanRule> sawmillRule;

	public static final ItemGroup SZ_GROUP = FabricItemGroupBuilder.build();

	public static Identifier id(String name) {
		return new Identifier(MODID, name);
	}

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
	public void onInitialize() {
		understandRule = GameRuleRegistry.register("szwUnderstand", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));

		quiverRule = GameRuleRegistry.register("szwQuiver", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(true, SZWorkshop::verifyWorldChanges));
		sawmillRule = GameRuleRegistry.register("szwSawmill", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true, SZWorkshop::verifyWorldChanges));

		SZBlocks.initialize();
		SZItems.initialize();
		SZEntities.initialize();
		SZRecipes.initialize();
		SZScreenHandlers.initialize();
		SZSoundEvents.initialize();
	}
}
