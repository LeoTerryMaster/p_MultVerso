package l2.gameserver.network.l2.c2s;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.model.pledge.UnitMember;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.NickNameChanged;
import l2.gameserver.utils.Util;

public class RequestGiveNickName extends L2GameClientPacket
{
	private String _target;
	private String _title;
	
	@Override
	protected void readImpl()
	{
		_target = readS(Config.CNAME_MAXLEN);
		_title = readS();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(!_title.isEmpty() && !Util.isMatchingRegexp(_title, Config.CLAN_TITLE_TEMPLATE))
		{
			activeChar.sendMessage("Incorrect title.");
			return;
		}
		if(activeChar.isNoble() && _target.equals(activeChar.getName()))
		{
			activeChar.setTitle(_title);
			activeChar.sendPacket(Msg.TITLE_HAS_CHANGED);
			activeChar.broadcastPacket(new NickNameChanged(activeChar));
			return;
		}
		if((activeChar.getClanPrivileges() & 4) != 4)
		{
			return;
		}
		if(activeChar.getClan().getLevel() < 3)
		{
			activeChar.sendPacket(Msg.TITLE_ENDOWMENT_IS_ONLY_POSSIBLE_WHEN_CLANS_SKILL_LEVELS_ARE_ABOVE_3);
			return;
		}
		UnitMember member = activeChar.getClan().getAnyMember(_target);
		if(member != null)
		{
			member.setTitle(_title);
			if(member.isOnline())
			{
				member.getPlayer().sendPacket(Msg.TITLE_HAS_CHANGED);
				member.getPlayer().sendChanges();
			}
		}
		else
		{
			activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestGiveNickName.NotInClan", activeChar));
		}
	}
}