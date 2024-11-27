package l2.gameserver.handler.admincommands.impl;

import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.ChatType;
import l2.gameserver.network.l2.s2c.Say2;
import l2.gameserver.tables.GmListTable;

public class AdminGmChat implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().CanAnnounce)
		{
			return false;
		}
		switch(command)
		{
			case admin_gmchat:
			{
				try
				{
					String text = fullString.replaceFirst(Commands.admin_gmchat.name(), "");
					Say2 cs = new Say2(0, ChatType.ALLIANCE, activeChar.getName(), text);
					GmListTable.broadcastToGMs(cs);
					break;
				}
				catch(StringIndexOutOfBoundsException e)
				{
					break;
				}
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
		admin_gmchat,
		admin_snoop;
	}
}