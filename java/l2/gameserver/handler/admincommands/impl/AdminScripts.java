package l2.gameserver.handler.admincommands.impl;

import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.scripts.Scripts;

public class AdminScripts implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.isGM())
		{
			return false;
		}
		switch(command)
		{
			case admin_scripts_reload:
			case admin_sreload:
			{
				if(wordList.length < 2)
				{
					return false;
				}
				String param = wordList[1];
				if(param.equalsIgnoreCase("all"))
				{
					activeChar.sendMessage("Scripts reload starting...");
					if(!Scripts.getInstance().reload())
					{
						activeChar.sendMessage("Scripts reloaded with errors. Loaded " + Scripts.getInstance().getClasses().size() + " classes.");
						break;
					}
					activeChar.sendMessage("Scripts successfully reloaded. Loaded " + Scripts.getInstance().getClasses().size() + " classes.");
					break;
				}
				if(!Scripts.getInstance().reload(param))
				{
					activeChar.sendMessage("Script(s) reloaded with errors.");
					break;
				}
				activeChar.sendMessage("Script(s) successfully reloaded.");
				break;
			}
			case admin_sqreload:
			{
				if(wordList.length < 2)
				{
					return false;
				}
				String quest = wordList[1];
				if(!Scripts.getInstance().reload("quests/" + quest))
				{
					activeChar.sendMessage("Quest \"" + quest + "\" reloaded with errors.");
				}
				else
				{
					activeChar.sendMessage("Quest \"" + quest + "\" successfully reloaded.");
				}
				reloadQuestStates(activeChar);
			}
		}
		return true;
	}
	
	private void reloadQuestStates(Player p)
	{
		for(QuestState qs : p.getAllQuestsStates())
		{
			p.removeQuestState(qs.getQuest().getName());
		}
		Quest.restoreQuestStates(p);
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	private enum Commands
	{
		admin_scripts_reload,
		admin_sreload,
		admin_sqreload;
	}
}