package l2.gameserver.network.l2.c2s;

import l2.gameserver.Config;
import l2.gameserver.model.Player;

public class RequestOlympiadObserverEnd extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(Config.OLY_ENABLED && activeChar.isOlyObserver())
		{
			activeChar.leaveOlympiadObserverMode();
		}
	}
}