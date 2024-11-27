package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.events.impl.SiegeEvent;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.pledge.SubUnit;
import l2.gameserver.model.pledge.UnitMember;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.PledgeShowMemberListDelete;
import l2.gameserver.network.l2.s2c.PledgeShowMemberListDeleteAll;
import l2.gameserver.network.l2.s2c.SystemMessage;

public class RequestWithdrawalPledge extends L2GameClientPacket
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
		if(activeChar.getClanId() == 0)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInCombat())
		{
			activeChar.sendPacket(Msg.ONE_CANNOT_LEAVE_ONES_CLAN_DURING_COMBAT);
			return;
		}
		Clan clan = activeChar.getClan();
		if(clan == null)
		{
			return;
		}
		UnitMember member = clan.getAnyMember(activeChar.getObjectId());
		if(member == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		SubUnit mainUnit = clan.getSubUnit(0);
		if(member.isClanLeader() || mainUnit.getNextLeaderObjectId() == member.getObjectId())
		{
			activeChar.sendPacket(SystemMsg.A_CLAN_LEADER_CANNOT_WITHDRAW_FROM_THEIR_OWN_CLAN);
			return;
		}
		activeChar.removeEventsByClass(SiegeEvent.class);
		int subUnitType = activeChar.getPledgeType();
		clan.removeClanMember(subUnitType, activeChar.getObjectId());
		clan.broadcastToOnlineMembers(new SystemMessage(SystemMsg.S1_HAS_WITHDRAWN_FROM_THE_CLAN).addString(activeChar.getName()), new PledgeShowMemberListDelete(activeChar.getName()));
		if(subUnitType == -1)
		{
			activeChar.setLvlJoinedAcademy(0);
		}
		activeChar.setClan(null);
		if(!activeChar.isNoble())
		{
			activeChar.setTitle("");
		}
		activeChar.setLeaveClanCurTime();
		activeChar.broadcastCharInfo();
		activeChar.sendPacket(SystemMsg.YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN, PledgeShowMemberListDeleteAll.STATIC);
	}
}