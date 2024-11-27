package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.model.pledge.Alliance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.tables.ClanTable;

public class RequestOustAlly extends L2GameClientPacket
{
	private String _clanName;
	
	@Override
	protected void readImpl()
	{
		_clanName = readS(32);
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		Clan leaderClan = activeChar.getClan();
		if(leaderClan == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		Alliance alliance = leaderClan.getAlliance();
		if(alliance == null)
		{
			activeChar.sendPacket(Msg.YOU_ARE_NOT_CURRENTLY_ALLIED_WITH_ANY_CLANS);
			return;
		}
		if(!activeChar.isAllyLeader())
		{
			activeChar.sendPacket(Msg.FEATURE_AVAILABLE_TO_ALLIANCE_LEADERS_ONLY);
			return;
		}
		if(_clanName == null)
		{
			return;
		}
		Clan clan = ClanTable.getInstance().getClanByName(_clanName);
		if(clan != null)
		{
			if(!alliance.isMember(clan.getClanId()))
			{
				activeChar.sendActionFailed();
				return;
			}
			if(alliance.getLeader().equals(clan))
			{
				activeChar.sendPacket(Msg.YOU_HAVE_FAILED_TO_WITHDRAW_FROM_THE_ALLIANCE);
				return;
			}
			clan.broadcastToOnlineMembers(new SystemMessage("Your clan has been expelled from " + alliance.getAllyName() + " alliance."), new SystemMessage(468));
			clan.setAllyId(0);
			clan.setLeavedAlly();
			alliance.broadcastAllyStatus();
			alliance.removeAllyMember(clan.getClanId());
			alliance.setExpelledMember();
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustAlly.ClanDismissed", activeChar).addString(clan.getName()).addString(alliance.getAllyName()));
		}
	}
}