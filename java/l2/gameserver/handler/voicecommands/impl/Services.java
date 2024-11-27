package l2.gameserver.handler.voicecommands.impl;

import l2.gameserver.Config;
import l2.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.scripts.Functions;

public class Services extends Functions implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands = {"autoloot", "xpfreez", "ru", "en"};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if((command = command.intern()).startsWith("autoloot") && target != null && target.length() > 1)
		{
			autoLoot(activeChar, target.startsWith("on"), target.startsWith("adena"));
			return true;
		}
		if(command.startsWith("xpfreez"))
		{
			if(target.startsWith("on"))
			{
				activeChar.setVar("NoExp", "1", -1);
				activeChar.sendMessage("Exp freezed.");
				return true;
			}
			if(target.startsWith("off"))
			{
				activeChar.unsetVar("NoExp");
				activeChar.sendMessage("Exp normal.");
				return true;
			}
		}
		else
		{
			if(command.startsWith("ru"))
			{
				activeChar.setVar("lang@", "ru", -1);
				return true;
			}
			if(command.startsWith("en"))
			{
				activeChar.setVar("lang@", "en", -1);
				return true;
			}
		}
		return false;
	}
	
	public void autoLoot(Player player, boolean on, boolean adena)
	{
		if(on && !adena)
		{
			player.setAutoLoot(on);
			if(Config.AUTO_LOOT_HERBS)
			{
				player.setAutoLootHerbs(on);
			}
			player.sendMessage("Autolooting all.");
		}
		else if(adena)
		{
			player.setAutoLoot(false);
			player.setAutoLootAdena(adena);
			player.sendMessage("Autolooting adena only.");
		}
		else
		{
			player.setAutoLootAdena(false);
			player.setAutoLoot(false);
			player.setAutoLootHerbs(false);
			player.sendMessage("Autolooting off.");
		}
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}