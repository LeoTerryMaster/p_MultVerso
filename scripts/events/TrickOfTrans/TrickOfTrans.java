package events.TrickOfTrans;

import l2.commons.util.Rnd;
import l2.gameserver.Announcements;
import l2.gameserver.Config;
import l2.gameserver.listener.actor.OnDeathListener;
import l2.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.SimpleSpawner;
import l2.gameserver.model.actor.listener.CharListenerList;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class TrickOfTrans extends Functions implements ScriptFile, OnDeathListener, OnPlayerEnterListener
{
	private static final Logger _log = LoggerFactory.getLogger(TrickOfTrans.class);
	private static final int EVENT_MANAGER_ID = 32132;
	private static final int CHESTS_ID = 13036;
	private static final int RED_PSTC = 9162;
	private static final int BLUE_PSTC = 9163;
	private static final int ORANGE_PSTC = 9164;
	private static final int BLACK_PSTC = 9165;
	private static final int WHITE_PSTC = 9166;
	private static final int GREEN_PSTC = 9167;
	private static final int RED_PSTC_R = 9171;
	private static final int BLUE_PSTC_R = 9172;
	private static final int ORANGE_PSTC_R = 9173;
	private static final int BLACK_PSTC_R = 9174;
	private static final int WHITE_PSTC_R = 9175;
	private static final int GREEN_PSTC_R = 9176;
	private static final int A_CHEST_KEY = 9205;
	private static final ArrayList<SimpleSpawner> _em_spawns = new ArrayList();
	private static final ArrayList<SimpleSpawner> _ch_spawns = new ArrayList();
	private static final int PhilosophersStoneOre = 9168;
	private static final int PhilosophersStoneOreMax = 17;
	private static final int PhilosophersStoneConversionFormula = 9169;
	private static final int MagicReagents = 9170;
	private static final int MagicReagentsMax = 30;
	private static boolean _active;
	
	private static boolean isActive()
	{
		return IsActive("trickoftrans");
	}
	
	@Override
	public void onLoad()
	{
		CharListenerList.addGlobal(this);
		if(isActive())
		{
			_active = true;
			spawnEventManagers();
			_log.info("Loaded Event: Trick of Trnasmutation [state: activated]");
		}
		else
		{
			_log.info("Loaded Event: Trick of Trnasmutation [state: deactivated]");
		}
	}
	
	public void startEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
		{
			return;
		}
		if(SetActive("trickoftrans", true))
		{
			spawnEventManagers();
			System.out.println("Event 'Trick of Transmutation' started.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.TrickOfTrans.AnnounceEventStarted", null);
		}
		else
		{
			player.sendMessage("Event 'Trick of Transmutation' already started.");
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
		if(SetActive("trickoftrans", false))
		{
			unSpawnEventManagers();
			System.out.println("Event 'Trick of Transmutation' stopped.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.TrickOfTrans.AnnounceEventStoped", null);
		}
		else
		{
			player.sendMessage("Event 'Trick of Transmutation' not started.");
		}
		_active = false;
		show("admin/events/events.htm", player);
	}
	
	@Override
	public void onPlayerEnter(Player player)
	{
		if(_active)
		{
			Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.TrickOfTrans.AnnounceEventStarted", null);
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
	
	private void spawnEventManagers()
	{
		int[][] EVENT_MANAGERS = {{147992, 28616, -2295, 0}, {81919, 148290, -3472, 51432}, {18293, 145208, -3081, 6470}, {-14694, 122699, -3122, 0}, {-81634, 150275, -3155, 15863}};
		SpawnNPCs(EVENT_MANAGER_ID, EVENT_MANAGERS, _em_spawns);
		int[][] CHESTS = {{148081, 28614, -2274, 2059}, {147918, 28615, -2295, 31471}, {147998, 28534, -2274, 49152}, {148053, 28550, -2274, 55621}, {147945, 28563, -2274, 40159}, {82012, 148286, -3472, 61567}, {81822, 148287, -3493, 29413}, {81917, 148207, -3493, 49152}, {81978, 148228, -3472, 53988}, {81851, 148238, -3472, 40960}, {18343, 145253, -3096, 7449}, {18284, 145274, -3090, 19740}, {18351, 145186, -3089, 61312}, {18228, 145265, -3079, 21674}, {18317, 145140, -3078, 55285}, {-14584, 122694, -3122, 65082}, {-14610, 122756, -3143, 13029}, {-14628, 122627, -3122, 50632}, {-14697, 122607, -3143, 48408}, {-14686, 122787, -3122, 12416}, {-81745, 150275, -3134, 32768}, {-81520, 150275, -3134, 0}, {-81628, 150379, -3134, 16025}, {-81696, 150347, -3155, 22854}, {-81559, 150332, -3134, 3356}};
		SpawnNPCs(CHESTS_ID, CHESTS, _ch_spawns);
	}
	
	private void unSpawnEventManagers()
	{
		deSpawnNPCs(_em_spawns);
		deSpawnNPCs(_ch_spawns);
	}
	
	@Override
	public void onDeath(Creature cha, Creature killer)
	{
		if(_active && SimpleCheckDrop(cha, killer) && Rnd.chance(Config.EVENT_TRICK_OF_TRANS_CHANCE * killer.getPlayer().getRateItems() * Config.RATE_DROP_ITEMS * ((NpcInstance) cha).getTemplate().rateHp))
		{
			((NpcInstance) cha).dropItem(killer.getPlayer(), A_CHEST_KEY, 1);
		}
	}
	
	public void accept()
	{
		Player player = getSelf();
		if(!player.isQuestContinuationPossible(true))
		{
			return;
		}
		if(!player.findRecipe(RED_PSTC_R))
		{
			addItem(player, RED_PSTC, (long) 1);
		}
		if(!player.findRecipe(BLACK_PSTC_R))
		{
			addItem(player, BLACK_PSTC, (long) 1);
		}
		if(!player.findRecipe(BLUE_PSTC_R))
		{
			addItem(player, BLUE_PSTC, (long) 1);
		}
		if(!player.findRecipe(GREEN_PSTC_R))
		{
			addItem(player, GREEN_PSTC, (long) 1);
		}
		if(!player.findRecipe(ORANGE_PSTC_R))
		{
			addItem(player, ORANGE_PSTC, (long) 1);
		}
		if(!player.findRecipe(WHITE_PSTC_R))
		{
			addItem(player, WHITE_PSTC, (long) 1);
		}
		show("scripts/events/TrickOfTrans/TrickOfTrans_01.htm", player);
	}
	
	public void open()
	{
		Player player = getSelf();
		if(getItemCount(player, A_CHEST_KEY) > 0)
		{
			removeItem(player, A_CHEST_KEY, (long) 1);
			addItem(player, PhilosophersStoneOre, (long) Rnd.get(1, PhilosophersStoneOreMax));
			addItem(player, MagicReagents, (long) Rnd.get(1, MagicReagentsMax));
			if(Rnd.chance(80))
			{
				addItem(player, PhilosophersStoneConversionFormula, (long) 1);
			}
			show("scripts/events/TrickOfTrans/TrickOfTrans_02.htm", player);
		}
		else
		{
			show("scripts/events/TrickOfTrans/TrickOfTrans_03.htm", player);
		}
	}
}