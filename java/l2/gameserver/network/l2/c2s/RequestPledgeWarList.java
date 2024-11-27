package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.s2c.PledgeReceiveWarList;

public class RequestPledgeWarList extends L2GameClientPacket
{
	private int _type;
	private int _page;
	
	@Override
	protected void readImpl()
	{
		_page = readD();
		_type = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		Clan clan = activeChar.getClan();
		if(clan != null)
		{
			activeChar.sendPacket(new PledgeReceiveWarList(clan, _type, _page));
		}
	}
}