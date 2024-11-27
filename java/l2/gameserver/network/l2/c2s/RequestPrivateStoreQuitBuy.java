package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;

public class RequestPrivateStoreQuitBuy extends L2GameClientPacket
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
		if(!activeChar.isInStoreMode() || activeChar.getPrivateStoreType() != 3)
		{
			activeChar.sendActionFailed();
			return;
		}
		activeChar.setPrivateStoreType(0);
		activeChar.standUp();
		activeChar.broadcastCharInfo();
	}
}