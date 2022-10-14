package me.aurelium.szworkshop.recipe;

import me.aurelium.szworkshop.block.SZBlocks;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CuttingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class SawmillRecipe extends CuttingRecipe {
	public SawmillRecipe(Identifier id, String group, Ingredient input, ItemStack output) {
		super(SZRecipes.SAWMILL, RecipeSerializer.STONECUTTING, id, group, input, output);
	}

	@Override
	public boolean isIgnoredInRecipeBook() {
		return true;
	}

	@Override
	public boolean matches(Inventory inventory, World world) {
		return this.input.test(inventory.getStack(0));
	}

	@Override
	public ItemStack createIcon() {
		return new ItemStack(SZBlocks.SAWMILL);
	}
}
