package me.aurelium.szworkshop.recipe;

import me.aurelium.szworkshop.SZWorkshop;
import net.minecraft.recipe.CuttingRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SZRecipes {
	public static RecipeSerializer<SawmillRecipe> SAWMILL_SERIALIZER;
	public static RecipeType<SawmillRecipe> SAWMILL;

	public static void initialize() {
		SAWMILL_SERIALIZER = Registry.register(Registry.RECIPE_SERIALIZER, SZWorkshop.id( "saw_mill"), new CuttingRecipe.Serializer<>(SawmillRecipe::new));
		SAWMILL = Registry.register(Registry.RECIPE_TYPE, SZWorkshop.id( "saw_mill"), new RecipeType<SawmillRecipe>() {
			public String toString() {
				return "saw_mill";
			}
		});
	}
}
