package events.l2day;

import l2.commons.util.Rnd;
import l2.gameserver.Announcements;
import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.listener.actor.OnDeathListener;
import l2.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.SimpleSpawner;
import l2.gameserver.model.actor.listener.CharListenerList;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.reward.RewardData;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.templates.npc.NpcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class LettersCollection extends Functions implements ScriptFile, OnDeathListener, OnPlayerEnterListener
{
	private static final Logger _log = LoggerFactory.getLogger(LettersCollection.class);
	protected static boolean _active;
	protected static String _name;
	protected static int[][] letters;
	protected static int[][] EVENT_MANAGERS;
	protected static String _msgStarted;
	protected static String _msgEnded;
	protected static int A;
	protected static int C;
	protected static int E;
	protected static int F;
	protected static int G;
	protected static int H;
	protected static int I;
	protected static int L;
	protected static int N;
	protected static int O;
	protected static int R;
	protected static int S;
	protected static int T;
	protected static int II;
	protected static int EVENT_MANAGER_ID;
	protected static Map<String, Integer[][]> _words;
	protected static Map<String, RewardData[]> _rewards;
	protected static List<SimpleSpawner> _spawns;
	
	static
	{
		EVENT_MANAGERS = null;
		A = 3875;
		C = 3876;
		E = 3877;
		F = 3878;
		G = 3879;
		H = 3880;
		I = 3881;
		L = 3882;
		N = 3883;
		O = 3884;
		R = 3885;
		S = 3886;
		T = 3887;
		II = 3888;
		EVENT_MANAGER_ID = 31230;
		_words = new HashMap<>();
		_rewards = new HashMap<>();
		_spawns = new ArrayList<>();
	}
	
	protected static boolean isActive()
	{
		return IsActive(_name);
	}
	
	@Override
	public void onLoad()
	{
		CharListenerList.addGlobal(this);
		if(isActive())
		{
			_active = true;
			spawnEventManagers();
			_log.info("Loaded Event: " + _name + " [state: activated]");
		}
		else
		{
			_log.info("Loaded Event: " + _name + " [state: deactivated]");
		}
	}
	
	protected void spawnEventManagers()
	{
		SpawnNPCs(EVENT_MANAGER_ID, EVENT_MANAGERS, _spawns);
	}
	
	protected void unSpawnEventManagers()
	{
		deSpawnNPCs(_spawns);
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
	
	@Override
	public void onDeath(Creature cha, Creature killer)
	{
		int[] letter;
		if(_active && SimpleCheckDrop(cha, killer) && Rnd.chance((double) (letter = letters[Rnd.get(letters.length)])[1] * Config.EVENT_L2DAY_LETTER_CHANCE * ((NpcTemplate) cha.getTemplate()).rateHp))
		{
			((NpcInstance) cha).dropItem(killer.getPlayer(), letter[0], 1);
		}
	}
	
	public void startEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
		{
			return;
		}
		if(SetActive(_name, true))
		{
			spawnEventManagers();
			System.out.println("Event '" + _name + "' started.");
			Announcements.getInstance().announceByCustomMessage(_msgStarted, null);
		}
		else
		{
			player.sendMessage("Event '" + _name + "' already started.");
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
		if(SetActive(_name, false))
		{
			unSpawnEventManagers();
			System.out.println("Event '" + _name + "' stopped.");
			Announcements.getInstance().announceByCustomMessage(_msgEnded, null);
		}
		else
		{
			player.sendMessage("Event '" + _name + "' not started.");
		}
		_active = false;
		show("admin/events/events.htm", player);
	}
	
	public void exchange(String[] var)
	{
		Player player = getSelf();
		if(!player.isQuestContinuationPossible(true))
		{
			return;
		}
		if(!NpcInstance.canBypassCheck(player, player.getLastNpc()))
		{
			return;
		}
		Integer[][] mss;
		for(Integer[] l : mss = _words.get(var[0]))
		{
			if(getItemCount(player, l[0]) >= (long) l[1].intValue())
				continue;
			player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
			return;
		}
		for(Integer[] l : mss)
		{
			removeItem(player, l[0], (long) l[1].intValue());
		}
		RewardData[] rewards = _rewards.get(var[0]);
		int sum = 0;
		for(RewardData r : rewards)
		{
			sum = (int) ((double) sum + r.getChance());
		}
		int random = Rnd.get(sum);
		sum = 0;
		for(RewardData r : rewards)
		{
			if((sum = (int) ((double) sum + r.getChance())) <= random)
				continue;
			addItem(player, r.getItemId(), Rnd.get(r.getMinDrop(), r.getMaxDrop()));
			return;
		}
	}
	
	@Override
	public void onPlayerEnter(Player player)
	{
		if(_active)
		{
			Announcements.getInstance().announceToPlayerByCustomMessage(player, _msgStarted, null);
		}
	}
	
	public String DialogAppend_31230(Integer val)
	{
		if(!_active)
		{
			return "";
		}
		StringBuilder append = new StringBuilder("<br><br>");
		for(String word : _words.keySet())
		{
			append.append("[scripts_").append(getClass().getName()).append(":exchange ").append(word).append("|").append(word).append("]<br1>");
		}
		return append.toString();
	}
}