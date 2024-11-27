package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;

public class RequestPrivateStoreQuitSell extends L2GameClientPacket
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
		if(!activeChar.isInStoreMode() || activeChar.getPrivateStoreType() != 1 && activeChar.getPrivateStoreType() != 8)
		{
			activeChar.sendActionFailed();
			return;
		}
		activeChar.setPrivateStoreType(0);
		activeChar.standUp();
		activeChar.broadcastCharInfo();
	}
}