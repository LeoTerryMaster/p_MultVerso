package l2.gameserver.handler.usercommands;

import l2.gameserver.model.Player;

public interface IUserCommandHandler
{
	boolean useUserCommand(int id, Player activeChar);
	
	int[] getUserCommandList();
}