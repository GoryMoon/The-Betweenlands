package thebetweenlands.common.registries;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Throwables;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import thebetweenlands.api.recipes.IAnimatorRecipe;
import thebetweenlands.api.recipes.ICompostBinRecipe;
import thebetweenlands.api.recipes.IDruidAltarRecipe;
import thebetweenlands.api.recipes.IPestleAndMortarRecipe;
import thebetweenlands.api.recipes.IPurifierRecipe;
import thebetweenlands.common.recipe.custom.CustomAnimatorRecipes;
import thebetweenlands.common.recipe.custom.CustomCompostBinRecipes;
import thebetweenlands.common.recipe.custom.CustomDruidAltarRecipes;
import thebetweenlands.common.recipe.custom.CustomPestleAndMortarRecipes;
import thebetweenlands.common.recipe.custom.CustomPurifierRecipes;
import thebetweenlands.common.recipe.custom.CustomRecipes;
import thebetweenlands.common.recipe.custom.CustomRecipes.InvalidRecipeException;
import thebetweenlands.util.config.ConfigHandler;

public class CustomRecipeRegistry {
	private CustomRecipeRegistry() { }

	private static final List<CustomRecipes<?>> RECIPE_TYPES = new ArrayList<>();

	public static CustomRecipes<IAnimatorRecipe> animatorRecipes;
	public static CustomRecipes<IPurifierRecipe> purifiedRecipes;
	public static CustomRecipes<ICompostBinRecipe> compostBinRecipes;
	public static CustomRecipes<IDruidAltarRecipe> druidAltarRecipes;
	public static CustomRecipes<IPestleAndMortarRecipe> pestleAndMortarRecipes;

	public static void preInit() {
		RECIPE_TYPES.add(animatorRecipes = new CustomAnimatorRecipes());
		RECIPE_TYPES.add(purifiedRecipes = new CustomPurifierRecipes());
		RECIPE_TYPES.add(compostBinRecipes = new CustomCompostBinRecipes());
		RECIPE_TYPES.add(druidAltarRecipes = new CustomDruidAltarRecipes());
		RECIPE_TYPES.add(pestleAndMortarRecipes = new CustomPestleAndMortarRecipes());
	}

	public static void loadCustomRecipes() {
		unregisterCustomRecipes();

		for(CustomRecipes<?> recipe : RECIPE_TYPES) {
			recipe.clear();
		}

		File cfgFile = new File(ConfigHandler.path);
		File customRecipesFile = new File(cfgFile.getParentFile(), "thebetweenlands" + File.separator + "recipes.json");
		if(customRecipesFile.exists()) {
			try(JsonReader jsonReader = new JsonReader(new FileReader(customRecipesFile))) {
				JsonObject jsonObj = new JsonParser().parse(jsonReader).getAsJsonObject();
				for(CustomRecipes<?> recipes : RECIPE_TYPES) {
					if(jsonObj.has(recipes.getName())) {
						try {
							JsonArray arr = jsonObj.get(recipes.getName()).getAsJsonArray();
							recipes.parse(arr);
						} catch(InvalidRecipeException ex) {
							ex.printStackTrace();
						}
					}
				}
			} catch (Exception e) {
				Throwables.propagate(e);
			}
		}

		registerCustomRecipes();
	}

	public static void registerCustomRecipes() {
		for(CustomRecipes<?> recipes : RECIPE_TYPES) {
			recipes.registerRecipes();
		}
	}

	public static void unregisterCustomRecipes() {
		for(CustomRecipes<?> recipes : RECIPE_TYPES) {
			recipes.unregisterRecipes();
		}
	}
}
