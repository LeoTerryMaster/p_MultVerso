package l2.gameserver.handler.admincommands;

import l2.gameserver.model.Player;

public interface IAdminCommandHandler
{
	boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar);
	
	Enum[] getAdminCommandEnum();
}