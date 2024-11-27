package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.RecipeShopSellList;

public class RequestRecipeShopSellList extends L2GameClientPacket
{
	int _manufacturerId;
	
	@Override
	protected void readImpl()
	{
		_manufacturerId = readD();
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
		activeChar.sendPacket(new RecipeShopSellList(activeChar, manufacturer));
	}
}