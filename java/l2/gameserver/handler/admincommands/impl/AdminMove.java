package l2.gameserver.handler.admincommands.impl;

import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.model.Player;

public class AdminMove implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().CanReload)
		{
			return false;
		}
		switch(command)
		{
			case admin_move_debug:
			{
				if(wordList.length > 1)
				{
					int dbgMode = Integer.parseInt(wordList[1]);
					if(dbgMode > 0)
					{
						activeChar.setVar("debugMove", Integer.parseInt(wordList[1]), -1);
						activeChar.sendMessage("Move debug mode " + dbgMode);
						break;
					}
					activeChar.unsetVar("debugMove");
					activeChar.sendMessage("Move debug disabled");
					break;
				}
				activeChar.setVar("debugMove", activeChar.getVarInt("debugMove", 0) > 0 ? 0 : 1, -1);
				activeChar.sendMessage("Move debug mode " + activeChar.getVarInt("debugMove", 0));
			}
		}
		return false;
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	private enum Commands
	{
		admin_move_debug;
	}
}