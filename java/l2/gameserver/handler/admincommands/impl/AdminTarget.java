package l2.gameserver.handler.admincommands.impl;

import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;

public class AdminTarget implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().CanViewChar)
		{
			return false;
		}
		try
		{
			String targetName = wordList[1];
			Player obj = World.getPlayer(targetName);
			if(obj != null && obj.isPlayer())
			{
				obj.onAction(activeChar, false);
			}
			else
			{
				activeChar.sendMessage("Player " + targetName + " not found");
			}
		}
		catch(IndexOutOfBoundsException e)
		{
			activeChar.sendMessage("Please specify correct name.");
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
		admin_target;
	}
}