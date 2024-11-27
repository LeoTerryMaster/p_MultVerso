package l2.gameserver.handler.voicecommands.impl;

import l2.gameserver.Config;
import l2.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.scripts.Functions;

public class Online extends Functions implements IVoicedCommandHandler
{
	private final String[] _commandList = {"online"};
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
	
	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if(Config.SERVICES_ONLINE_COMMAND_ENABLE || player.isGM())
		{
			player.sendMessage("Players Online: " + GameObjectsStorage.getAllPlayersCount() * Config.SERVICE_COMMAND_MULTIPLIER);
			return false;
		}
		return true;
	}
}