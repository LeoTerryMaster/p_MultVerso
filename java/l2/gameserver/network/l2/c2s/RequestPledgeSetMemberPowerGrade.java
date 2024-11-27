package l2.gameserver.network.l2.c2s;

import l2.gameserver.model.Player;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.pledge.UnitMember;
import l2.gameserver.network.l2.components.CustomMessage;

public class RequestPledgeSetMemberPowerGrade extends L2GameClientPacket
{
	private int _powerGrade;
	private String _name;
	
	@Override
	protected void readImpl()
	{
		_name = readS(16);
		_powerGrade = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(_powerGrade < 1 || _powerGrade > 9)
		{
			return;
		}
		Clan clan = activeChar.getClan();
		if(clan == null)
		{
			return;
		}
		if((activeChar.getClanPrivileges() & 16) == 16)
		{
			UnitMember member = activeChar.getClan().getAnyMember(_name);
			if(member != null)
			{
				if(Clan.isAcademy(member.getPledgeType()))
				{
					activeChar.sendMessage("You cannot change academy member grade.");
					return;
				}
				if(_powerGrade > 5)
				{
					member.setPowerGrade(clan.getAffiliationRank(member.getPledgeType()));
				}
				else
				{
					member.setPowerGrade(_powerGrade);
				}
				if(member.isOnline())
				{
					member.getPlayer().sendUserInfo();
				}
			}
			else
			{
				activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestPledgeSetMemberPowerGrade.NotBelongClan", activeChar));
			}
		}
		else
		{
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestPledgeSetMemberPowerGrade.HaveNotAuthority", activeChar));
		}
	}
}