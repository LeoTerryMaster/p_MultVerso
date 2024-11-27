package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;

public class RequestExCancelEnchantItem extends L2GameClientPacket
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
			activeChar.setEnchantScroll(null);
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS);
		}
	}
}