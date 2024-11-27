package l2.gameserver.handler.admincommands.impl;

import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;

public class AdminEvents implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().IsEventGm)
		{
			return false;
		}
		switch(command)
		{
			case admin_events:
			{
				if(wordList.length == 1)
				{
					activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/events/events.htm"));
					break;
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/events/" + wordList[1].trim()));
			}
		}
		return true;
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	private enum Commands
	{
		admin_events;
	}
}