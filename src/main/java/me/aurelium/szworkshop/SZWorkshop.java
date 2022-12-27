package me.aurelium.szworkshop;

import me.aurelium.szworkshop.block.SZBlocks;
import me.aurelium.szworkshop.enchantments.SZEnchantments;
import me.aurelium.szworkshop.entity.SZEntities;
import me.aurelium.szworkshop.item.SZItems;
import me.aurelium.szworkshop.recipe.SZRecipes;
import me.aurelium.szworkshop.sound.SZSoundEvents;
import me.aurelium.szworkshop.ui.screen.SZScreenHandlers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.SetEnchantmentsLootFunction;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerProfession;
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
	public static GameRules.Key<GameRules.BooleanRule> sifterRule;
	public static GameRules.Key<GameRules.BooleanRule> glowsquidFishingRule;
	public static GameRules.Key<GameRules.BooleanRule> stompingEnchantmentRule;
	public static GameRules.Key<GameRules.BooleanRule> orbanglerEnchantmentRule;
	public static GameRules.Key<GameRules.BooleanRule> multicatchEnchantmentRule;

	public static final ItemGroup SZ_GROUP = FabricItemGroupBuilder.create(id("main")).icon(() -> new ItemStack(SZBlocks.SIFTER)).appendItems(stacks -> {
		stacks.add(EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(SZEnchantments.STOMPING, 1)));
		stacks.add(EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(SZEnchantments.STOMPING, 2)));
		stacks.add(EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(SZEnchantments.STOMPING, 3)));
		stacks.add(EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(SZEnchantments.ORB_ANGLER, 1)));
		stacks.add(EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(SZEnchantments.MULTICATCH, 1)));
		stacks.add(new ItemStack(SZItems.GLOW_SQUID_BUCKET));
		stacks.add(new ItemStack(SZBlocks.SIFTER));
		stacks.add(new ItemStack(SZBlocks.SAWMILL));
	}).build();

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
		sifterRule = GameRuleRegistry.register("szwSifter", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true, SZWorkshop::verifyWorldChanges));
		glowsquidFishingRule = GameRuleRegistry.register("szwGlowsquidFishing", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
		stompingEnchantmentRule = GameRuleRegistry.register("szwStomping", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
		orbanglerEnchantmentRule = GameRuleRegistry.register("szwOrbAngler", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
		multicatchEnchantmentRule = GameRuleRegistry.register("szwMulticatch", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));

		LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
			// Add Stomping enchantment to mineshaft chests.
			if (source.isBuiltin() && LootTables.ABANDONED_MINESHAFT_CHEST.equals(id)) {
				LootPool.Builder poolBuilder = LootPool.builder()
					.with(ItemEntry.builder(Items.ENCHANTED_BOOK).apply(new SetEnchantmentsLootFunction.Builder().enchantment(SZEnchantments.STOMPING, UniformLootNumberProvider.create(1, 1))));

				tableBuilder.pool(poolBuilder);
			}

			// Add enchantments to shipwreck chests.
			if (source.isBuiltin() && LootTables.SHIPWRECK_TREASURE_CHEST.equals(id)) {
				LootPool.Builder poolBuilder = LootPool.builder();
					poolBuilder.with(ItemEntry.builder(Items.ENCHANTED_BOOK).apply(new SetEnchantmentsLootFunction.Builder().enchantment(SZEnchantments.ORB_ANGLER, UniformLootNumberProvider.create(1, 1))));
					poolBuilder.with(ItemEntry.builder(Items.ENCHANTED_BOOK).apply(new SetEnchantmentsLootFunction.Builder().enchantment(SZEnchantments.MULTICATCH, UniformLootNumberProvider.create(1, 1))));

				tableBuilder.pool(poolBuilder);
			}



		});

		SZBlocks.initialize();
		SZItems.initialize();
		SZEntities.initialize();
		SZRecipes.initialize();
		SZScreenHandlers.initialize();
		SZSoundEvents.initialize();
		SZEnchantments.initialize();
	}
}
