package events.heart;

import l2.commons.text.PrintfFormat;
import l2.commons.util.Rnd;
import l2.gameserver.Announcements;
import l2.gameserver.Config;
import l2.gameserver.data.htm.HtmCache;
import l2.gameserver.listener.actor.OnDeathListener;
import l2.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.SimpleSpawner;
import l2.gameserver.model.actor.listener.CharListenerList;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class heart extends Functions implements ScriptFile, OnDeathListener, OnPlayerEnterListener
{
	private static final Logger _log = LoggerFactory.getLogger(heart.class);
	private static final List<SimpleSpawner> _spawns = new ArrayList<>();
	private static final Map<Integer, Integer> Guesses = new HashMap<>();
	private static final String[][] variants = {{"Rock", "Камень"}, {"Scissors", "Ножницы"}, {"Paper", "Бумага"}};
	private static final int EVENT_MANAGER_ID = 31227;
	private static final int[] hearts;
	private static final int[] potions;
	private static final int[] scrolls;
	private static boolean _active;
	private static String links_en = "";
	private static String links_ru = "";
	
	static
	{
		PrintfFormat fmt = new PrintfFormat("<br><a action=\"bypass -h scripts_events.heart.heart:play %d\">\"%s!\"</a>");
		for(int i = 0;i < variants.length;++i)
		{
			links_en = links_en + fmt.sprintf(i, variants[i][0]);
			links_ru = links_ru + fmt.sprintf(i, variants[i][1]);
		}
		hearts = new int[] {4209, 4210, 4211, 4212, 4213, 4214, 4215, 4216, 4217};
		potions = new int[] {1374, 1375, 6036, 1539};
		scrolls = new int[] {3926, 3927, 3928, 3929, 3930, 3931, 3932, 3933, 3934, 3935};
	}
	
	private static boolean isActive()
	{
		return IsActive("heart");
	}
	
	public void startEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
		{
			return;
		}
		if(SetActive("heart", true))
		{
			spawnEventManagers();
			System.out.println("Event 'Change of Heart' started.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.ChangeofHeart.AnnounceEventStarted", null);
		}
		else
		{
			player.sendMessage("Event 'Change of Heart' already started.");
		}
		_active = true;
		show("admin/events/events.htm", player);
	}
	
	public void stopEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
		{
			return;
		}
		if(SetActive("heart", false))
		{
			unSpawnEventManagers();
			System.out.println("Event 'Change of Heart' stopped.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.ChangeofHeart.AnnounceEventStoped", null);
		}
		else
		{
			player.sendMessage("Event 'Change of Heart' not started.");
		}
		_active = false;
		show("admin/events/events.htm", player);
	}
	
	public void letsplay()
	{
		Player player = getSelf();
		if(!player.isQuestContinuationPossible(true))
		{
			return;
		}
		zeroGuesses(player);
		if(haveAllHearts(player))
		{
			show(link(HtmCache.getInstance().getNotNull("scripts/events/heart/hearts_01.htm", player), isRus(player)), player);
		}
		else
		{
			show("scripts/events/heart/hearts_00.htm", player);
		}
	}
	
	public void play(String[] var)
	{
		Player player = getSelf();
		if(!player.isQuestContinuationPossible(true) || var.length == 0)
		{
			return;
		}
		if(!haveAllHearts(player))
		{
			if(var[0].equalsIgnoreCase("Quit"))
			{
				show("scripts/events/heart/hearts_00b.htm", player);
			}
			else
			{
				show("scripts/events/heart/hearts_00a.htm", player);
			}
			return;
		}
		if(var[0].equalsIgnoreCase("Quit"))
		{
			int curr_guesses = getGuesses(player);
			takeHeartsSet(player);
			reward(player, curr_guesses);
			show("scripts/events/heart/hearts_reward_" + curr_guesses + ".htm", player);
			zeroGuesses(player);
			return;
		}
		int var_cat = Rnd.get(variants.length);
		int var_player = 0;
		try
		{
			var_player = Integer.parseInt(var[0]);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return;
		}
		if(var_player == var_cat)
		{
			show(fillvars(HtmCache.getInstance().getNotNull("scripts/events/heart/hearts_same.htm", player), var_player, var_cat, player), player);
			return;
		}
		if(playerWins(var_player, var_cat))
		{
			incGuesses(player);
			int curr_guesses = getGuesses(player);
			if(curr_guesses == 10)
			{
				takeHeartsSet(player);
				reward(player, 10);
				zeroGuesses(player);
			}
			show(fillvars(HtmCache.getInstance().getNotNull("scripts/events/heart/hearts_level_" + curr_guesses + ".htm", player), var_player, var_cat, player), player);
			return;
		}
		takeHeartsSet(player);
		reward(player, getGuesses(player) - 1);
		show(fillvars(HtmCache.getInstance().getNotNull("scripts/events/heart/hearts_loose.htm", player), var_player, var_cat, player), player);
		zeroGuesses(player);
	}
	
	private void reward(Player player, int guesses)
	{
		switch(guesses)
		{
			case -1:
			case 0:
			{
				addItem(player, scrolls[Rnd.get(scrolls.length)], (long) 1);
				break;
			}
			case 1:
			{
				addItem(player, potions[Rnd.get(potions.length)], (long) 10);
				break;
			}
			case 2:
			{
				addItem(player, 1538, (long) 1);
				break;
			}
			case 3:
			{
				addItem(player, 3936, (long) 1);
				break;
			}
			case 4:
			{
				addItem(player, 951, (long) 2);
				break;
			}
			case 5:
			{
				addItem(player, 948, (long) 4);
				break;
			}
			case 6:
			{
				addItem(player, 947, (long) 1);
				break;
			}
			case 7:
			{
				addItem(player, 730, (long) 3);
				break;
			}
			case 8:
			{
				addItem(player, 729, (long) 1);
				break;
			}
			case 9:
			{
				addItem(player, 960, (long) 2);
				break;
			}
			case 10:
			{
				addItem(player, 959, (long) 1);
			}
		}
	}
	
	private String fillvars(String s, int var_player, int var_cat, Player player)
	{
		boolean rus;
		return link(s.replaceFirst("Player", player.getName()).replaceFirst("%var_payer%", variants[var_player][(rus = isRus(player)) ? 1 : 0]).replaceFirst("%var_cat%", variants[var_cat][rus ? 1 : 0]), rus);
	}
	
	private boolean isRus(Player player)
	{
		return player.isLangRus();
	}
	
	private String link(String s, boolean rus)
	{
		return s.replaceFirst("%links%", rus ? links_ru : links_en);
	}
	
	private boolean playerWins(int var_player, int var_cat)
	{
		if(var_player == 0)
		{
			return var_cat == 1;
		}
		if(var_player == 1)
		{
			return var_cat == 2;
		}
		if(var_player == 2)
		{
			return var_cat == 0;
		}
		return false;
	}
	
	private int getGuesses(Player player)
	{
		return Guesses.containsKey(player.getObjectId()) ? Guesses.get(player.getObjectId()) : 0;
	}
	
	private void incGuesses(Player player)
	{
		int val = 1;
		if(Guesses.containsKey(player.getObjectId()))
		{
			val = Guesses.remove(player.getObjectId()) + 1;
		}
		Guesses.put(player.getObjectId(), val);
	}
	
	private void zeroGuesses(Player player)
	{
		if(Guesses.containsKey(player.getObjectId()))
		{
			Guesses.remove(player.getObjectId());
		}
	}
	
	private void takeHeartsSet(Player player)
	{
		for(int heart_id : hearts)
		{
			removeItem(player, heart_id, (long) 1);
		}
	}
	
	private boolean haveAllHearts(Player player)
	{
		for(int heart_id : hearts)
		{
			if(player.getInventory().getCountOf(heart_id) >= 1)
				continue;
			return false;
		}
		return true;
	}
	
	@Override
	public void onDeath(Creature cha, Creature killer)
	{
		if(_active && SimpleCheckDrop(cha, killer))
		{
			((NpcInstance) cha).dropItem(killer.getPlayer(), hearts[Rnd.get(hearts.length)], Util.rollDrop((long) 1, (long) 1, Config.EVENT_CHANGE_OF_HEART_CHANCE * killer.getPlayer().getRateItems() * ((MonsterInstance) cha).getTemplate().rateHp * 10000.0, true));
		}
	}
	
	@Override
	public void onPlayerEnter(Player player)
	{
		if(_active)
		{
			Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.ChangeofHeart.AnnounceEventStarted", null);
		}
	}
	
	private void spawnEventManagers()
	{
		int[][] EVENT_MANAGERS = {{146936, 26654, -2208, 16384}, {82168, 148842, -3464, 7806}, {82204, 53259, -1488, 16384}, {18924, 145782, -3088, 44034}, {111794, 218967, -3536, 20780}, {-14539, 124066, -3112, 50874}, {147271, -55573, -2736, 60304}, {87801, -143150, -1296, 28800}, {-80684, 149458, -3040, 16384}};
		SpawnNPCs(31227, EVENT_MANAGERS, _spawns);
	}
	
	private void unSpawnEventManagers()
	{
		deSpawnNPCs(_spawns);
	}
	
	@Override
	public void onLoad()
	{
		CharListenerList.addGlobal(this);
		if(isActive())
		{
			_active = true;
			spawnEventManagers();
			_log.info("Loaded Event: Change of Heart [state: activated]");
		}
		else
		{
			_log.info("Loaded Event: Change of Heart[state: deactivated]");
		}
	}
	
	@Override
	public void onReload()
	{
		unSpawnEventManagers();
	}
	
	@Override
	public void onShutdown()
	{
		unSpawnEventManagers();
	}
}