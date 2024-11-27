package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;

public class RequestObserverEnd extends L2GameClientPacket
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
		if(activeChar.isInObserverMode())
		{
			if(activeChar.isOlyObserver())
			{
				activeChar.leaveOlympiadObserverMode();
			}
			else
			{
				activeChar.leaveObserverMode();
			}
		}
	}
}