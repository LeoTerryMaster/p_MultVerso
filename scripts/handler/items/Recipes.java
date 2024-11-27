package handler.items;

import l2.gameserver.cache.Msg;
import l2.gameserver.data.xml.holder.RecipeHolder;
import l2.gameserver.model.Playable;
import l2.gameserver.model.Player;
import l2.gameserver.model.Recipe;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.RecipeBookItemList;
import l2.gameserver.network.l2.s2c.SystemMessage;

import java.util.Collection;

public class Recipes extends ScriptItemHandler
{
	private static int[] _itemIds;
	
	public Recipes()
	{
		Collection<Recipe> rc = RecipeHolder.getInstance().getRecipes();
		_itemIds = new int[rc.size()];
		int i = 0;
		for(Recipe r : rc)
		{
			_itemIds[i++] = r.getItem().getItemId();
		}
	}
	
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
		{
			return false;
		}
		Player player = (Player) playable;
		Recipe recipe = RecipeHolder.getInstance().getRecipeByItem(item);
		switch(recipe.getType())
		{
			case ERT_DWARF:
			{
				if(player.getDwarvenRecipeLimit() > 0)
				{
					if(player.getDwarvenRecipeBook().size() >= player.getDwarvenRecipeLimit())
					{
						player.sendPacket(Msg.NO_FURTHER_RECIPES_MAY_BE_REGISTERED);
						return false;
					}
					if(recipe.getRequiredSkillLvl() > player.getSkillLevel(Integer.valueOf(172)))
					{
						player.sendPacket(Msg.CREATE_ITEM_LEVEL_IS_TOO_LOW_TO_REGISTER_THIS_RECIPE);
						return false;
					}
					if(player.hasRecipe(recipe))
					{
						player.sendPacket(Msg.THAT_RECIPE_IS_ALREADY_REGISTERED);
						return false;
					}
					if(!player.getInventory().destroyItem(item, 1))
					{
						player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
						return false;
					}
					player.registerRecipe(recipe, true);
					player.sendPacket(new SystemMessage(851).addItemName(item.getItemId()));
					player.sendPacket(new RecipeBookItemList(player, true));
					return true;
				}
				player.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_REGISTER_A_RECIPE);
				break;
			}
			case ERT_COMMON:
			{
				if(player.getCommonRecipeLimit() > 0)
				{
					if(player.getCommonRecipeBook().size() >= player.getCommonRecipeLimit())
					{
						player.sendPacket(Msg.NO_FURTHER_RECIPES_MAY_BE_REGISTERED);
						return false;
					}
					if(player.hasRecipe(recipe))
					{
						player.sendPacket(Msg.THAT_RECIPE_IS_ALREADY_REGISTERED);
						return false;
					}
					if(!player.getInventory().destroyItem(item, 1))
					{
						player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
						return false;
					}
					player.registerRecipe(recipe, true);
					player.sendPacket(new SystemMessage(851).addItemName(item.getItemId()));
					player.sendPacket(new RecipeBookItemList(player, false));
					return true;
				}
				player.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_REGISTER_A_RECIPE);
			}
		}
		return false;
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}