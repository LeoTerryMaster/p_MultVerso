package l2.gameserver.network.l2.c2s;

import l2.gameserver.Config;
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

public class RequestOustPledgeMember extends L2GameClientPacket
{
	private String _target;
	
	@Override
	protected void readImpl()
	{
		_target = readS(Config.CNAME_MAXLEN);
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || (activeChar.getClanPrivileges() & 64) != 64)
		{
			return;
		}
		Clan clan = activeChar.getClan();
		UnitMember member = clan.getAnyMember(_target);
		if(member == null)
		{
			activeChar.sendPacket(SystemMsg.THE_TARGET_MUST_BE_A_CLAN_MEMBER);
			return;
		}
		SubUnit mainUnit = clan.getSubUnit(0);
		Player memberPlayer = member.getPlayer();
		if(member.isOnline() && member.getPlayer().isInCombat())
		{
			activeChar.sendPacket(SystemMsg.A_CLAN_MEMBER_MAY_NOT_BE_DISMISSED_DURING_COMBAT);
			return;
		}
		if(member.isClanLeader() || mainUnit.getNextLeaderObjectId() == member.getObjectId())
		{
			activeChar.sendPacket(SystemMsg.A_CLAN_LEADER_CANNOT_WITHDRAW_FROM_THEIR_OWN_CLAN);
			return;
		}
		int subUnitType = member.getPledgeType();
		clan.removeClanMember(subUnitType, member.getObjectId());
		clan.broadcastToOnlineMembers(new SystemMessage(191).addString(_target), new PledgeShowMemberListDelete(_target));
		if(subUnitType != -1)
		{
			clan.setExpelledMember();
		}
		if(memberPlayer == null)
		{
			return;
		}
		memberPlayer.removeEventsByClass(SiegeEvent.class);
		if(subUnitType == -1)
		{
			memberPlayer.setLvlJoinedAcademy(0);
		}
		memberPlayer.setClan(null);
		if(!memberPlayer.isNoble())
		{
			memberPlayer.setTitle("");
		}
		memberPlayer.setLeaveClanCurTime();
		memberPlayer.broadcastCharInfo();
		memberPlayer.broadcastRelationChanged();
		memberPlayer.store(true);
		memberPlayer.sendPacket(Msg.YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN_YOU_ARE_NOT_ALLOWED_TO_JOIN_ANOTHER_CLAN_FOR_24_HOURS, PledgeShowMemberListDeleteAll.STATIC);
	}
}