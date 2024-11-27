package events.Halloween;

import l2.commons.util.Rnd;
import l2.gameserver.Announcements;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;

public class Halloween extends Functions implements ScriptFile
{
	private static final Logger _log = LoggerFactory.getLogger(Halloween.class);
	private static final String EVENT_NAME = "HalloweenEvent";
	private static final int[][] GHOST_SPAWN = {{81945, 148597, -3472, 600, 15}, {147456, 27480, -2229, 500, 12}, {82610, 55643, -1550, 200, 6}, {18650, 145436, -3153, 350, 9}, {111389, 220161, -3700, 420, 10}, {-14225, 123540, -3121, 200, 7}, {147723, -56388, -2807, 150, 5}, {87356, -141767, -1344, 220, 7}};
	private static final String[] SKILLDIE_SPAWN = {"Hot Springs:147607,-114850,-2002", "Rainbow Springs Chateau:152587,-126642,-2315", "Forge of the Gods:167693,-113024,-2844", "Wall of Argos:176338,-49702,-3334", "Swamp of Screams:94637,-60140,-2475", "Restless Forest:65725,-48176,-2823", "Valley of Saints:66767,-73106,-3718", "Windtail Waterfall:41303,-92076,-3703", "Cursed Village:57129,-41255,-3177", "Stakato Nest:88413,-43714,-2193", "Beast Farm:44735,-87574,-2578", "Grave Robber Hideout:46146,-106761,-1516", "Crypts of Disgrace:43242,-120040,-3408", "Den of Evil:67288,-112072,-2176", "Archaic Laboratory:92168,-115144,-3344", "Plunderous Plains:127736,-149416,-3736", "Brigand Stronghold:124520,-161496,-1168", "Deamon Fortress:100006,-52612,-673", "Borderland Fortress:155951,-70319,-2804", "Lost Nest:24044,-10452,-2589", "Primeval Plains Waterfall:6810,-12014,-3674", "Mimir's Forest:-82051,51084,-3339", "Chromatic Highlands:154735,152587,-3684"};
	private static final String en = "<br1>[scripts_events.Halloween.Halloween:show|\"Halloween Event\"]<br1>";
	private static final String ru = "<br1>[scripts_events.Halloween.Halloween:show|\"Ивент Хэллоуин\"]<br1>";
	private static final ArrayList<NpcInstance> _ghostSpawn;
	private static boolean _active;
	private static ScheduledFuture<?> _eventTask;
	private static NpcInstance SkooldieInstance;
	
	static
	{
		_ghostSpawn = new ArrayList();
		SkooldieInstance = null;
	}
	
	private static boolean isActive()
	{
		return IsActive("HalloweenEvent");
	}
	
	public static void OnPlayerEnter(Player player)
	{
		if(_active)
		{
			Announcements.getInstance().announceToPlayerByCustomMessage(player, "scripts.events.Halloween.EventActive", null);
		}
	}
	
	public static void SpawnGhosts(int idx)
	{
		ArrayList<Location> loc_list = new ArrayList<>();
		double step = 6.283185307179586 / (double) GHOST_SPAWN[idx][4];
		for(int i = 0;i < GHOST_SPAWN[idx][4];++i)
		{
			double rad = step * (double) i;
			loc_list.add(new Location(GHOST_SPAWN[idx][0] + (int) ((double) GHOST_SPAWN[idx][3] * Math.cos(rad)), GHOST_SPAWN[idx][1] + (int) ((double) GHOST_SPAWN[idx][3] * Math.sin(rad)), GHOST_SPAWN[idx][2]));
		}
		Location[] locs = loc_list.toArray(new Location[loc_list.size()]);
		loc_list.clear();
		for(int i = 0;i < locs.length;++i)
		{
			NpcInstance npc = NpcHolder.getInstance().getTemplate(Config.EVENT_PUMPKIN_GHOST_ID).getNewInstance();
			npc.setAI(new PumpkinGhostAI(npc, locs, i, Config.EVENT_PUMPKIN_DROP_CHANCE, Config.EVENT_PUMPKIN_DROP_ITEMS));
			npc.setFlying(false);
			npc.setTargetable(false);
			npc.setShowName(false);
			npc.spawnMe(locs[i]);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			_ghostSpawn.add(npc);
		}
		Announcements.getInstance().announceByCustomMessage("scripts.events.Halloween.PumpkinGhost.spawn." + idx, null);
	}
	
	public static void DespawnGhost()
	{
		if(_ghostSpawn == null || _ghostSpawn.isEmpty())
		{
			return;
		}
		for(NpcInstance npc : _ghostSpawn)
		{
			npc.deleteMe();
		}
		_ghostSpawn.clear();
	}
	
	public static void RunEvent()
	{
		SkooldieDespawn();
		DespawnGhost();
		SpawnGhosts(Rnd.get(GHOST_SPAWN.length));
		executeTask("events.Halloween.Halloween", "DespawnGhost", new Object[0], (long) Config.EVENT_PUMPKIN_GHOST_SHOW_TIME);
		executeTask("events.Halloween.Halloween", "SkooldieSpawn", new Object[0], (long) (Config.EVENT_PUMPKIN_GHOST_SHOW_TIME - 30000));
		executeTask("events.Halloween.Halloween", "SkooldieDespawn", new Object[0], (long) (Config.EVENT_PUMPKIN_GHOST_SHOW_TIME + Config.EVENT_SKOOLDIE_TIME));
	}
	
	public static void SkooldieSpawn()
	{
		if(SkooldieInstance != null)
		{
			SkooldieInstance.deleteMe();
		}
		int point = Rnd.get(SKILLDIE_SPAWN.length);
		String[] spwn = SKILLDIE_SPAWN[point].split(":");
		SkooldieInstance = Functions.spawn(Location.parseLoc(spwn[1]), Config.EVENT_SKOOLDIE_REWARDER);
		Announcements.getInstance().announceByCustomMessage("scripts.events.Halloween.SkooldieSpawned", new String[] {spwn[0]});
	}
	
	public static void SkooldieDespawn()
	{
		if(SkooldieInstance != null)
		{
			SkooldieInstance.deleteMe();
		}
		SkooldieInstance = null;
	}
	
	public static void start()
	{
		stop();
		_eventTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new EventRunner(), 120000, (long) Config.EVENT_PUMPKIN_DELAY);
	}
	
	public static void stop()
	{
		if(_eventTask != null)
		{
			_eventTask.cancel(true);
			_eventTask = null;
		}
		DespawnGhost();
		SkooldieDespawn();
	}
	
	public void startEvent()
	{
		Player player = getSelf();
		if(!player.getPlayerAccess().IsEventGm)
		{
			return;
		}
		if(SetActive("HalloweenEvent", true))
		{
			start();
			_log.info("Event: 'HalloweenEvent' started.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.Halloween.AnnounceEventStarted", null);
		}
		else
		{
			player.sendMessage("Event 'HalloweenEvent' already started.");
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
		DespawnGhost();
		SkooldieDespawn();
		if(SetActive("HalloweenEvent", false))
		{
			stop();
			_log.info("Event: 'HalloweenEvent' stopped.");
			Announcements.getInstance().announceByCustomMessage("scripts.events.Halloween.AnnounceEventStoped", null);
		}
		else
		{
			player.sendMessage("Event 'HalloweenEvent' not started.");
		}
		_active = false;
		show("admin/events/events.htm", player);
	}
	
	public String DialogAppend_40029(Integer val)
	{
		return getHtmlAppends(val);
	}
	
	public String getHtmlAppends(Integer val)
	{
		if(!_active || val != 0)
		{
			return "";
		}
		Player player = getSelf();
		if(player == null)
		{
			return "";
		}
		return player.isLangRus() ? "<br1>[scripts_events.Halloween.Halloween:show|\"Ивент Хэллоуин\"]<br1>" : "<br1>[scripts_events.Halloween.Halloween:show|\"Halloween Event\"]<br1>";
	}
	
	public void show()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
		{
			return;
		}
		show("scripts/events/halloween/exchange.htm", player);
	}
	
	public void exchange()
	{
		Player player = getSelf();
		NpcInstance npc = getNpc();
		if(player == null || npc == null)
		{
			return;
		}
		long cnt = Functions.getItemCount(player, Config.EVENT_HALLOWEEN_CANDY);
		if(cnt > 0)
		{
			Functions.removeItem(player, Config.EVENT_HALLOWEEN_CANDY, (long) Config.EVENT_HALLOWEEN_CANDY_ITEM_COUNT_NEEDED);
			Functions.addItem(player, Config.EVENT_HALLOWEEN_TOY_CHEST, (long) Config.EVENT_HALLOWEEN_TOY_CHEST_REWARD_AMOUNT);
			Announcements.getInstance().announceByCustomMessage("scripts.events.Halloween.SkooldieFind", new String[] {player.getName()});
			SkooldieDespawn();
			DespawnGhost();
		}
		else
		{
			show("scripts/events/halloween/noitem.htm", player);
		}
	}
	
	@Override
	public void onLoad()
	{
		if(isActive())
		{
			_active = true;
			start();
			_log.info("Loaded Event: 'HalloweenEvent' [state: activated]");
		}
		else
		{
			_log.info("Loaded Event: 'HalloweenEvent' [state: deactivated]");
		}
	}
	
	@Override
	public void onReload()
	{
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	public static class EventRunner implements Runnable
	{
		@Override
		public void run()
		{
			RunEvent();
		}
	}
}