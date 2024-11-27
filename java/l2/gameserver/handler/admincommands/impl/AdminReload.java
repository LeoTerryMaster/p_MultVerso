package l2.gameserver.handler.admincommands.impl;

import l2.commons.threading.RunnableImpl;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.data.StringHolder;
import l2.gameserver.data.htm.HtmCache;
import l2.gameserver.data.xml.holder.BuyListHolder;
import l2.gameserver.data.xml.holder.MultiSellHolder;
import l2.gameserver.data.xml.parser.ChatFilterParser;
import l2.gameserver.data.xml.parser.NpcParser;
import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.instancemanager.SpawnManager;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.oly.NoblesController;
import l2.gameserver.model.quest.Quest;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.tables.FishTable;
import l2.gameserver.tables.PetDataTable;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.utils.Strings;

public class AdminReload implements IAdminCommandHandler
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
			case admin_reload:
			{
				break;
			}
			case admin_reload_config:
			{
				try
				{
					Config.load();
				}
				catch(Exception e)
				{
					activeChar.sendMessage("Error: " + e.getMessage() + "!");
					return false;
				}
				activeChar.sendMessage("Config reloaded!");
				break;
			}
			case admin_reload_multisell:
			{
				try
				{
					MultiSellHolder.getInstance().reload();
				}
				catch(Exception e)
				{
					return false;
				}
				activeChar.sendMessage("Multisell list reloaded!");
				break;
			}
			case admin_reload_gmaccess:
			{
				try
				{
					Config.loadGMAccess();
					for(Player player : GameObjectsStorage.getAllPlayersForIterate())
					{
						if(!Config.EVERYBODY_HAS_ADMIN_RIGHTS)
						{
							player.setPlayerAccess(Config.gmlist.get(player.getObjectId()));
							continue;
						}
						player.setPlayerAccess(Config.gmlist.get(new Integer(0)));
					}
				}
				catch(Exception e)
				{
					return false;
				}
				activeChar.sendMessage("GMAccess reloaded!");
				break;
			}
			case admin_reload_htm:
			{
				HtmCache.getInstance().clear();
				activeChar.sendMessage("HTML cache clearned.");
				break;
			}
			case admin_reload_qr:
			{
				Config.loadQuestRateSettings();
				activeChar.sendMessage("Quest rates reloaded.");
				break;
			}
			case admin_reload_qs:
			{
				if(fullString.endsWith("all"))
				{
					for(Player p : GameObjectsStorage.getAllPlayersForIterate())
					{
						reloadQuestStates(p);
					}
					break;
				}
				GameObject t = activeChar.getTarget();
				if(t != null && t.isPlayer())
				{
					Player p = (Player) t;
					reloadQuestStates(p);
					break;
				}
				reloadQuestStates(activeChar);
				break;
			}
			case admin_reload_qs_help:
			{
				activeChar.sendMessage("");
				activeChar.sendMessage("Quest Help:");
				activeChar.sendMessage("reload_qs_help - This Message.");
				activeChar.sendMessage("reload_qs <selected target> - reload all quest states for target.");
				activeChar.sendMessage("reload_qs <no target or target is not player> - reload quests for self.");
				activeChar.sendMessage("reload_qs all - reload quests for all players in world.");
				activeChar.sendMessage("");
				break;
			}
			case admin_reload_skills:
			{
				SkillTable.getInstance().reload();
				break;
			}
			case admin_reload_npc:
			{
				NpcParser.getInstance().reload();
				break;
			}
			case admin_reload_spawn:
			{
				ThreadPoolManager.getInstance().execute(new RunnableImpl()
				{
					
					@Override
					public void runImpl() throws Exception
					{
						SpawnManager.getInstance().reloadAll();
					}
				});
				break;
			}
			case admin_reload_fish:
			{
				FishTable.getInstance().reload();
				break;
			}
			case admin_reload_translit:
			{
				Strings.reload();
				break;
			}
			case admin_reload_shops:
			{
				BuyListHolder.reload();
				break;
			}
			case admin_reload_static:
			{
				break;
			}
			case admin_reload_chatfilters:
			{
				ChatFilterParser.getInstance().reload();
				break;
			}
			case admin_reload_pets:
			{
				PetDataTable.getInstance().reload();
				break;
			}
			case admin_reload_locale:
			{
				StringHolder.getInstance().reload();
				break;
			}
			case admin_reload_nobles:
			{
				NoblesController.getInstance().LoadNobleses();
			}
		}
		activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/reload.htm"));
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
		admin_reload,
		admin_reload_config,
		admin_reload_multisell,
		admin_reload_gmaccess,
		admin_reload_htm,
		admin_reload_qr,
		admin_reload_qs,
		admin_reload_qs_help,
		admin_reload_skills,
		admin_reload_npc,
		admin_reload_spawn,
		admin_reload_fish,
		admin_reload_chatfilters,
		admin_reload_translit,
		admin_reload_shops,
		admin_reload_static,
		admin_reload_pets,
		admin_reload_locale,
		admin_reload_nobles;
	}
}