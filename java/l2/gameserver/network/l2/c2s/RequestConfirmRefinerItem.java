package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.Msg;
import l2.gameserver.handler.items.IRefineryHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.ExPutIntensiveResultForVariationMake;

public class RequestConfirmRefinerItem extends L2GameClientPacket
{
	private int _targetItemObjId;
	private int _refinerItemObjId;
	
	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		ItemInstance mineralItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
		IRefineryHandler refineryHandler = activeChar.getRefineryHandler();
		if(targetItem == null || mineralItem == null || refineryHandler == null)
		{
			activeChar.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ExPutIntensiveResultForVariationMake.FAIL_PACKET);
			return;
		}
		refineryHandler.onPutMineralItem(activeChar, targetItem, mineralItem);
	}
}