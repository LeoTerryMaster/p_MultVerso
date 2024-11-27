package l2.gameserver.handler.usercommands.impl;

import l2.gameserver.cache.Msg;
import l2.gameserver.handler.usercommands.IUserCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.pledge.Alliance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.s2c.SystemMessage;

import java.util.ArrayList;
import java.util.List;

public class ClanWarsList implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = {88, 89, 90};
	
	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(id != COMMAND_IDS[0] && id != COMMAND_IDS[1] && id != COMMAND_IDS[2])
		{
			return false;
		}
		Clan clan = activeChar.getClan();
		if(clan == null)
		{
			activeChar.sendPacket(Msg.NOT_JOINED_IN_ANY_CLAN);
			return false;
		}
		List<Clan> data = new ArrayList();
		if(id == 88)
		{
			activeChar.sendPacket(Msg._ATTACK_LIST_);
			data = clan.getEnemyClans();
		}
		else if(id == 89)
		{
			activeChar.sendPacket(Msg._UNDER_ATTACK_LIST_);
			data = clan.getAttackerClans();
		}
		else
		{
			activeChar.sendPacket(Msg._WAR_LIST_);
			for(Clan c : clan.getEnemyClans())
			{
				if(!clan.getAttackerClans().contains(c))
					continue;
				data.add(c);
			}
		}
		for(Clan c : data)
		{
			String clanName = c.getName();
			Alliance alliance = c.getAlliance();
			SystemMessage sm = alliance != null ? new SystemMessage(1200).addString(clanName).addString(alliance.getAllyName()) : new SystemMessage(1202).addString(clanName);
			activeChar.sendPacket(sm);
		}
		activeChar.sendPacket(Msg.__EQUALS__);
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}