package handler.admincommands;

import l2.gameserver.model.GameObject;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.events.GlobalEvent;
import l2.gameserver.network.l2.components.SystemMsg;

public class AdminGlobalEvent extends ScriptAdminCommand
{
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands c = (Commands) comm;
		switch(c)
		{
			case admin_list_events:
			{
				GameObject object = activeChar.getTarget();
				if(object == null)
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					break;
				}
				for(GlobalEvent e : object.getEvents())
				{
					activeChar.sendMessage("- " + e);
				}
				break;
			}
		}
		return false;
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	enum Commands
	{
		admin_list_events;
	}
}