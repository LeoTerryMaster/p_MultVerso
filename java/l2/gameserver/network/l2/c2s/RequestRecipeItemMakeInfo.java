package l2.gameserver.network.l2.c2s;

import l2.gameserver.data.xml.holder.RecipeHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.Recipe;
import l2.gameserver.network.l2.s2c.RecipeItemMakeInfo;

public class RequestRecipeItemMakeInfo extends L2GameClientPacket
{
	private int _id;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		Recipe recipe = RecipeHolder.getInstance().getRecipeById(_id);
		if(recipe == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		sendPacket(new RecipeItemMakeInfo(activeChar, recipe, -1));
	}
}