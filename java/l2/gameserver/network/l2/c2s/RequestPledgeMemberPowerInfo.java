package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.pledge.UnitMember;
import l2.gameserver.network.l2.s2c.PledgeReceivePowerInfo;

public class RequestPledgeMemberPowerInfo extends L2GameClientPacket
{
	private int _not_known;
	private String _target;
	
	@Override
	protected void readImpl()
	{
		_not_known = readD();
		_target = readS(16);
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
		UnitMember cm;
		if(clan != null && (cm = clan.getAnyMember(_target)) != null)
		{
			activeChar.sendPacket(new PledgeReceivePowerInfo(cm));
		}
	}
}