package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.Msg;
import l2.gameserver.handler.items.IRefineryHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.ExVariationCancelResult;

public final class RequestRefineCancel extends L2GameClientPacket
{
	private int _targetItemObjId;
	
	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		ItemInstance item = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		IRefineryHandler refineryHandler = activeChar.getRefineryHandler();
		if(item == null || refineryHandler == null)
		{
			activeChar.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM, ExVariationCancelResult.FAIL_PACKET);
			return;
		}
		refineryHandler.onRequestCancelRefine(activeChar, item);
	}
}