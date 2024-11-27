package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.s2c.PledgeInfo;
import l2.gameserver.tables.ClanTable;

public class RequestPledgeInfo extends L2GameClientPacket
{
	private int _clanId;
	
	@Override
	protected void readImpl()
	{
		_clanId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(_clanId < 10000000)
		{
			activeChar.sendActionFailed();
			return;
		}
		Clan clan = ClanTable.getInstance().getClan(_clanId);
		if(clan == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		activeChar.sendPacket(new PledgeInfo(clan));
	}
}