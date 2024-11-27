package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.tables.GmListTable;

public class RequestGmList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar != null)
		{
			GmListTable.sendListToPlayer(activeChar);
		}
	}
}