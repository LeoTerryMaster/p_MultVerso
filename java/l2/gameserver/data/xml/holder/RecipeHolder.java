package l2.gameserver.data.xml.holder;

import l2.commons.data.xml.AbstractHolder;
import l2.gameserver.model.Recipe;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.templates.item.ItemTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class RecipeHolder extends AbstractHolder
{
	private static final Logger LOG = LoggerFactory.getLogger(RecipeHolder.class);
	private static final RecipeHolder INSTANCE = new RecipeHolder();
	private final Map<Integer, Recipe> _recipesById = new HashMap<>();
	private final Map<Integer, Recipe> _recipesByRecipeItemId = new HashMap<>();
	
	public static final RecipeHolder getInstance()
	{
		return INSTANCE;
	}
	
	public void addRecipe(Recipe recipe)
	{
		if(_recipesById.containsKey(recipe.getId()))
		{
			LOG.warn("Recipe \"" + recipe.getId() + "\" already exists.");
		}
		_recipesById.put(recipe.getId(), recipe);
		_recipesByRecipeItemId.put(recipe.getItem().getItemId(), recipe);
	}
	
	public Recipe getRecipeById(int recipeId)
	{
		return _recipesById.get(recipeId);
	}
	
	public Recipe getRecipeByItem(ItemTemplate itemTemplate)
	{
		return getRecipeByItem(itemTemplate.getItemId());
	}
	
	public Recipe getRecipeByItem(ItemInstance item)
	{
		return getRecipeByItem(item.getItemId());
	}
	
	public Recipe getRecipeByItem(int itemId)
	{
		return _recipesByRecipeItemId.get(itemId);
	}
	
	public Collection<Recipe> getRecipes()
	{
		return Collections.unmodifiableMap(_recipesById).values();
	}
	
	@Override
	public int size()
	{
		return _recipesById.size();
	}
	
	@Override
	public void clear()
	{
		_recipesById.clear();
		_recipesByRecipeItemId.clear();
	}
}