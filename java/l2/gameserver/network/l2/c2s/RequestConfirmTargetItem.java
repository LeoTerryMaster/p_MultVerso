package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.Msg;
import l2.gameserver.handler.items.IRefineryHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.ExPutItemResultForVariationMake;

public class RequestConfirmTargetItem extends L2GameClientPacket
{
	private int _itemObjId;
	
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
			activeChar.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ExPutItemResultForVariationMake.FAIL_PACKET);
			return;
		}
		refineryHandler.onPutTargetItem(activeChar, item);
	}
}