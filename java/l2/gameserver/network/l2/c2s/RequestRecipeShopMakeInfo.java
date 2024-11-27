package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.model.items.ManufactureItem;
import l2.gameserver.network.l2.s2c.RecipeShopItemInfo;

public class RequestRecipeShopMakeInfo extends L2GameClientPacket
{
	private int _manufacturerId;
	private int _recipeId;
	
	@Override
	protected void readImpl()
	{
		_manufacturerId = readD();
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
		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}
		Player manufacturer = (Player) activeChar.getVisibleObject(_manufacturerId);
		if(manufacturer == null || manufacturer.getPrivateStoreType() != 5 || !manufacturer.isInActingRange(activeChar))
		{
			activeChar.sendActionFailed();
			return;
		}
		long price = -1;
		for(ManufactureItem i : manufacturer.getCreateList())
		{
			if(i.getRecipeId() != _recipeId)
				continue;
			price = i.getCost();
			break;
		}
		if(price == -1)
		{
			activeChar.sendActionFailed();
			return;
		}
		activeChar.sendPacket(new RecipeShopItemInfo(activeChar, manufacturer, _recipeId, price, -1));
	}
}