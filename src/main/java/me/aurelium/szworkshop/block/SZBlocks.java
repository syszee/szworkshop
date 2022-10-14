package me.aurelium.szworkshop.block;

import me.aurelium.szworkshop.SZWorkshop;
import me.aurelium.szworkshop.recipe.SawmillRecipe;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SZBlocks {
	public static SawmillBlock SAWMILL;

	public static <T extends Block> T register(T block, String id) {
		Registry.register(Registry.BLOCK, new Identifier(SZWorkshop.MODID, id), block);
		Registry.register(Registry.ITEM, new Identifier(SZWorkshop.MODID, id), new BlockItem(block, new FabricItemSettings()));
		return block;
	}

	public static void initialize() {
		SAWMILL = register(new SawmillBlock(), "saw_mill");
	}
}
