package l2.gameserver.network.l2.c2s;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.pledge.SubUnit;
import l2.gameserver.model.pledge.UnitMember;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.PledgeShowMemberListUpdate;
import l2.gameserver.network.l2.s2c.SystemMessage;

public class RequestPledgeReorganizeMember extends L2GameClientPacket
{
	int _replace;
	String _subjectName;
	int _targetUnit;
	String _replaceName;
	
	@Override
	protected void readImpl()
	{
		_replace = readD();
		_subjectName = readS(16);
		_targetUnit = readD();
		if(_replace > 0)
		{
			_replaceName = readS();
		}
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
		if(clan == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(!activeChar.isClanLeader())
		{
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestPledgeReorganizeMember.ChangeAffiliations", activeChar));
			activeChar.sendActionFailed();
			return;
		}
		UnitMember subject = clan.getAnyMember(_subjectName);
		if(subject == null)
		{
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestPledgeReorganizeMember.NotInYourClan", activeChar));
			activeChar.sendActionFailed();
			return;
		}
		if(subject.getPledgeType() == _targetUnit)
		{
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestPledgeReorganizeMember.AlreadyInThatCombatUnit", activeChar));
			activeChar.sendActionFailed();
			return;
		}
		if(_targetUnit != 0 && clan.getSubUnit(_targetUnit) == null)
		{
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestPledgeReorganizeMember.NoSuchCombatUnit", activeChar));
			activeChar.sendActionFailed();
			return;
		}
		if(Clan.isAcademy(_targetUnit))
		{
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestPledgeReorganizeMember.AcademyViaInvitation", activeChar));
			activeChar.sendActionFailed();
			return;
		}
		if(Clan.isAcademy(subject.getPledgeType()))
		{
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestPledgeReorganizeMember.CantMoveAcademyMember", activeChar));
			activeChar.sendActionFailed();
			return;
		}
		UnitMember replacement = null;
		if(_replace > 0)
		{
			replacement = clan.getAnyMember(_replaceName);
			if(replacement == null)
			{
				activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestPledgeReorganizeMember.CharacterNotBelongClan", activeChar));
				activeChar.sendActionFailed();
				return;
			}
			if(replacement.getPledgeType() != _targetUnit)
			{
				activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestPledgeReorganizeMember.CharacterNotBelongCombatUnit", activeChar));
				activeChar.sendActionFailed();
				return;
			}
			if(replacement.isSubLeader() != 0)
			{
				activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestPledgeReorganizeMember.CharacterLeaderAnotherCombatUnit", activeChar));
				activeChar.sendActionFailed();
				return;
			}
		}
		else
		{
			if(clan.getUnitMembersSize(_targetUnit) >= clan.getSubPledgeLimit(_targetUnit))
			{
				if(_targetUnit == 0)
				{
					activeChar.sendPacket(new SystemMessage(1835).addString(clan.getName()));
				}
				else
				{
					activeChar.sendPacket(Msg.THE_ACADEMY_ROYAL_GUARD_ORDER_OF_KNIGHTS_IS_FULL_AND_CANNOT_ACCEPT_NEW_MEMBERS_AT_THIS_TIME);
				}
				activeChar.sendActionFailed();
				return;
			}
			if(subject.isSubLeader() != 0)
			{
				activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestPledgeReorganizeMember.MemberLeaderAnotherUnit", activeChar));
				activeChar.sendActionFailed();
				return;
			}
		}
		SubUnit oldUnit;
		if(replacement != null)
		{
			oldUnit = replacement.getSubUnit();
			oldUnit.replace(replacement.getObjectId(), subject.getPledgeType());
			clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(replacement));
			if(replacement.isOnline())
			{
				replacement.getPlayer().updatePledgeClass();
				replacement.getPlayer().broadcastCharInfo();
			}
		}
		oldUnit = subject.getSubUnit();
		oldUnit.replace(subject.getObjectId(), _targetUnit);
		clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(subject));
		if(subject.isOnline())
		{
			subject.getPlayer().updatePledgeClass();
			subject.getPlayer().broadcastCharInfo();
		}
	}
}