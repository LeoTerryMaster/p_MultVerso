package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.Msg;
import l2.gameserver.handler.items.IRefineryHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;

public class RequestConfirmCancelItem extends L2GameClientPacket
{
	int _itemObjId;
	
	@Override
	protected void readImpl()
	{
		_itemObjId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		ItemInstance item = activeChar.getInventory().getItemByObjectId(_itemObjId);
		IRefineryHandler refineryHandler = activeChar.getRefineryHandler();
		if(item == null || refineryHandler == null)
		{
			activeChar.sendActionFailed();
			activeChar.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}
		refineryHandler.onPutTargetCancelItem(activeChar, item);
	}
}