package me.aurelium.szworkshop.datagen;

import com.google.gson.JsonObject;
import me.aurelium.szworkshop.SZWorkshop;
import me.aurelium.szworkshop.recipe.SZRecipes;
import me.aurelium.szworkshop.recipe.SawmillRecipe;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.TagManagerLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import software.bernie.example.registry.ItemRegistry;

import java.util.function.Consumer;

public class SZDatagen implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		fabricDataGenerator.addProvider(SawmillWoodProvider::new);
	}

	public static class SawmillWoodProvider extends FabricRecipeProvider {

		public SawmillWoodProvider(FabricDataGenerator dataGenerator) {
			super(dataGenerator);
		}

		@Override
		protected void generateRecipes(Consumer<RecipeJsonProvider> exporter) {
			String[] logTypes = new String[] { "oak", "birch", "jungle", "dark_oak", "acacia", "crimson", "warped", "mangrove", "spruce"};

			for(String woodType : logTypes) {
				// Log -> Planks
				exporter.accept(new SawmillRecipeJsonProvider(
						new SawmillRecipe(
								new Identifier(SZWorkshop.MODID, "sawmill/" + woodType + "_planks"), "",
								Ingredient.ofItems(Registry.ITEM.get(new Identifier(woodType + "_log"))),
								new ItemStack(Registry.ITEM.get(new Identifier(woodType + "_planks")), 4)
						)
				));

				// Planks -> Stairs
				exporter.accept(new SawmillRecipeJsonProvider(
						new SawmillRecipe(
								new Identifier(SZWorkshop.MODID, "sawmill/" + woodType + "_stairs"), "",
								Ingredient.ofItems(Registry.ITEM.get(new Identifier(woodType + "_planks"))),
								new ItemStack(Registry.ITEM.get(new Identifier(woodType + "_stairs")), 1)
						)
				));

				// Planks -> Slabs
				exporter.accept(new SawmillRecipeJsonProvider(
						new SawmillRecipe(
								new Identifier(SZWorkshop.MODID, "sawmill/" + woodType + "_slab"), "",
								Ingredient.ofItems(Registry.ITEM.get(new Identifier(woodType + "_planks"))),
								new ItemStack(Registry.ITEM.get(new Identifier(woodType + "_slab")), 2)
						)
				));

				// Planks -> Fence
				exporter.accept(new SawmillRecipeJsonProvider(
						new SawmillRecipe(
								new Identifier(SZWorkshop.MODID, "sawmill/" + woodType + "_fence"), "",
								Ingredient.ofItems(Registry.ITEM.get(new Identifier(woodType + "_planks"))),
								new ItemStack(Registry.ITEM.get(new Identifier(woodType + "_fence")), 1)
						)
				));

				// Planks -> Fence Gate
				exporter.accept(new SawmillRecipeJsonProvider(
						new SawmillRecipe(
								new Identifier(SZWorkshop.MODID, "sawmill/" + woodType + "_fence_gate"), "",
								Ingredient.ofItems(Registry.ITEM.get(new Identifier(woodType + "_planks"))),
								new ItemStack(Registry.ITEM.get(new Identifier(woodType + "_fence_gate")), 1)
						)
				));

				// Planks -> Sign
				exporter.accept(new SawmillRecipeJsonProvider(
						new SawmillRecipe(
								new Identifier(SZWorkshop.MODID, "sawmill/" + woodType + "_sign"), "",
								Ingredient.ofItems(Registry.ITEM.get(new Identifier(woodType + "_planks"))),
								new ItemStack(Registry.ITEM.get(new Identifier(woodType + "_sign")), 1)
						)
				));
			}
		}
	}

	public static class SawmillRecipeJsonProvider implements RecipeJsonProvider {

		private final SawmillRecipe recipe;
		private final Advancement.Builder advancementBuilder = Advancement.Builder.create();
		private final Identifier advancementId;

		public SawmillRecipeJsonProvider(SawmillRecipe recipe) {
			this.recipe = recipe;
			this.advancementId = new Identifier(recipe.getId().getNamespace(), "recipes/" + this.recipe.getGroup() + "/" + this.recipe.getId().getPath());
		}

		@Override
		public void serialize(JsonObject json) {
			json.addProperty("count", recipe.getOutput().getCount());

			Ingredient ingredient = recipe.getIngredients().get(0);
			json.add("ingredient", ingredient.toJson());

			json.addProperty("result", Registry.ITEM.getId(recipe.getOutput().getItem()).toString());
		}

		@Override
		public Identifier getRecipeId() {
			return recipe.getId();
		}

		@Override
		public RecipeSerializer<?> getSerializer() {
			return SZRecipes.SAWMILL_SERIALIZER;
		}

		@Nullable
		@Override
		public JsonObject toAdvancementJson() {
			return advancementBuilder.toJson();
		}

		@Nullable
		@Override
		public Identifier getAdvancementId() {
			return this.advancementId;
		}
	}
}
