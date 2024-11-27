package l2.gameserver.handler.voicecommands;

import l2.gameserver.model.Player;

public interface IVoicedCommandHandler
{
	boolean useVoicedCommand(String command, Player player, String args);
	
	String[] getVoicedCommandList();
}