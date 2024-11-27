package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;

public class RequestExBuySellUIClose extends L2GameClientPacket
{
	@Override
	protected void runImpl()
	{
	}
	
	@Override
	protected void readImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		activeChar.setBuyListId(0);
		activeChar.sendItemList(true);
	}
}