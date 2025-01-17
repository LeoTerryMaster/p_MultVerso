package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.pledge.UnitMember;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.ActionFail;
import l2.gameserver.tables.ClanTable;

public class RequestStopPledgeWar extends L2GameClientPacket
{
	private String _pledgeName;
	
	@Override
	protected void readImpl()
	{
		_pledgeName = readS(32);
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		Clan playerClan = activeChar.getClan();
		if(playerClan == null)
		{
			return;
		}
		if((activeChar.getClanPrivileges() & 32) != 32)
		{
			activeChar.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT, ActionFail.STATIC);
			return;
		}
		Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
		if(clan == null)
		{
			activeChar.sendPacket(SystemMsg.CLAN_NAME_IS_INVALID, ActionFail.STATIC);
			return;
		}
		if(!playerClan.isAtWarWith(clan.getClanId()))
		{
			activeChar.sendPacket(Msg.YOU_HAVE_NOT_DECLARED_A_CLAN_WAR_TO_S1_CLAN, ActionFail.STATIC);
			return;
		}
		for(UnitMember mbr : playerClan)
		{
			if(!mbr.isOnline() || !mbr.getPlayer().isInCombat())
				continue;
			activeChar.sendPacket(Msg.A_CEASE_FIRE_DURING_A_CLAN_WAR_CAN_NOT_BE_CALLED_WHILE_MEMBERS_OF_YOUR_CLAN_ARE_ENGAGED_IN_BATTLE, ActionFail.STATIC);
			return;
		}
		ClanTable.getInstance().stopClanWar(playerClan, clan);
	}
}