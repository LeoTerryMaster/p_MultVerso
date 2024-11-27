package l2.gameserver.handler.bbs;

import l2.gameserver.model.Player;

public interface ICommunityBoardHandler
{
	String[] getBypassCommands();
	
	void onBypassCommand(Player player, String bypass);
	
	void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5);
}