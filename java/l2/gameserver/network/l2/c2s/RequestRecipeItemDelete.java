package l2.gameserver.network.l2.c2s;

import l2.gameserver.data.xml.holder.RecipeHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.Recipe;
import l2.gameserver.network.l2.s2c.RecipeBookItemList;

public class RequestRecipeItemDelete extends L2GameClientPacket
{
	private int _recipeId;
	
	@Override
	protected void readImpl()
	{
		_recipeId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(activeChar.getPrivateStoreType() == 5)
		{
			activeChar.sendActionFailed();
			return;
		}
		Recipe recipe = RecipeHolder.getInstance().getRecipeById(_recipeId);
		if(recipe == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		activeChar.unregisterRecipe(_recipeId);
		activeChar.sendPacket(new RecipeBookItemList(activeChar, recipe.getType() == Recipe.ERecipeType.ERT_DWARF));
	}
}