package bosses;

import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.idfactory.IdFactory;
import l2.gameserver.listener.actor.OnDeathListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Zone;
import l2.gameserver.model.actor.listener.CharListenerList;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.quest.QuestState;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.ReflectionUtils;
import npc.model.SepulcherNpcInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

public class FourSepulchersManager extends Functions implements ScriptFile
{
	public static final String QUEST_ID = "_620_FourGoblets";
	private static final Logger _log = LoggerFactory.getLogger(FourSepulchersManager.class);
	private static final Zone[] _zone = new Zone[4];
	private static final int ENTRANCE_PASS = 7075;
	private static final int USED_PASS = 7261;
	private static final int CHAPEL_KEY = 7260;
	private static final int ANTIQUE_BROOCH = 7262;
	private static final int _newCycleMin = 55;
	private static boolean _inEntryTime;
	private static boolean _inAttackTime;
	private static ScheduledFuture<?> _changeCoolDownTimeTask;
	private static ScheduledFuture<?> _changeEntryTimeTask;
	private static ScheduledFuture<?> _changeWarmUpTimeTask;
	private static ScheduledFuture<?> _changeAttackTimeTask;
	private static long _coolDownTimeEnd;
	private static long _entryTimeEnd;
	private static long _warmUpTimeEnd;
	private static long _attackTimeEnd;
	private static boolean _firstTimeRun;
	
	private static void timeSelector()
	{
		timeCalculator();
		long currentTime = System.currentTimeMillis();
		if(currentTime >= _coolDownTimeEnd && currentTime < _entryTimeEnd)
		{
			cleanUp();
			_changeEntryTimeTask = ThreadPoolManager.getInstance().schedule(new ChangeEntryTime(), 0);
			_log.info("FourSepulchersManager: Beginning in Entry time");
		}
		else if(currentTime >= _entryTimeEnd && currentTime < _warmUpTimeEnd)
		{
			cleanUp();
			_changeWarmUpTimeTask = ThreadPoolManager.getInstance().schedule(new ChangeWarmUpTime(), 0);
			_log.info("FourSepulchersManager: Beginning in WarmUp time");
		}
		else if(currentTime >= _warmUpTimeEnd && currentTime < _attackTimeEnd)
		{
			cleanUp();
			_changeAttackTimeTask = ThreadPoolManager.getInstance().schedule(new ChangeAttackTime(), 0);
			_log.info("FourSepulchersManager: Beginning in Attack time");
		}
		else
		{
			_changeCoolDownTimeTask = ThreadPoolManager.getInstance().schedule(new ChangeCoolDownTime(), 0);
			_log.info("FourSepulchersManager: Beginning in Cooldown time");
		}
	}
	
	private static void timeCalculator()
	{
		Calendar tmp = Calendar.getInstance();
		if(tmp.get(12) < _newCycleMin)
		{
			tmp.set(10, Calendar.getInstance().get(10) - 1);
		}
		tmp.set(12, _newCycleMin);
		_coolDownTimeEnd = tmp.getTimeInMillis();
		_entryTimeEnd = _coolDownTimeEnd + 180000;
		_warmUpTimeEnd = _entryTimeEnd + 120000;
		_attackTimeEnd = _warmUpTimeEnd + 3000000;
	}
	
	private static void cleanUp()
	{
		for(Player player : getPlayersInside())
		{
			player.teleToClosestTown();
		}
		FourSepulchersSpawn.deleteAllMobs();
		FourSepulchersSpawn.closeAllDoors();
		FourSepulchersSpawn._hallInUse.clear();
		FourSepulchersSpawn._hallInUse.put(31921, false);
		FourSepulchersSpawn._hallInUse.put(31922, false);
		FourSepulchersSpawn._hallInUse.put(31923, false);
		FourSepulchersSpawn._hallInUse.put(31924, false);
		if(!FourSepulchersSpawn._archonSpawned.isEmpty())
		{
			Set<Integer> npcIdSet = FourSepulchersSpawn._archonSpawned.keySet();
			for(int npcId : npcIdSet)
			{
				FourSepulchersSpawn._archonSpawned.put(npcId, false);
			}
		}
	}
	
	public static boolean isEntryTime()
	{
		return _inEntryTime;
	}
	
	public static boolean isAttackTime()
	{
		return _inAttackTime;
	}
	
	public static synchronized void tryEntry(NpcInstance npc, Player player)
	{
		int npcId = npc.getNpcId();
		switch(npcId)
		{
			case 31921:
			case 31922:
			case 31923:
			case 31924:
			{
				break;
			}
			default:
			{
				return;
			}
		}
		if(FourSepulchersSpawn._hallInUse.get(npcId).booleanValue())
		{
			showHtmlFile(player, "" + npcId + "-FULL.htm", npc, null);
			return;
		}
		if(!player.isInParty() || player.getParty().getMemberCount() < Config.FOUR_SEPULCHER_MIN_PARTY_MEMBERS)
		{
			showHtmlFile(player, "" + npcId + "-SP.htm", npc, null);
			return;
		}
		if(!player.getParty().isLeader(player))
		{
			showHtmlFile(player, "" + npcId + "-NL.htm", npc, null);
			return;
		}
		for(Player mem : player.getParty().getPartyMembers())
		{
			QuestState qs = mem.getQuestState("_620_FourGoblets");
			if(qs == null || !qs.isStarted() && !qs.isCompleted())
			{
				showHtmlFile(player, "" + npcId + "-NS.htm", npc, mem);
				return;
			}
			if(mem.getInventory().getItemByItemId(7075) == null)
			{
				showHtmlFile(player, "" + npcId + "-SE.htm", npc, mem);
				return;
			}
			if(!mem.isQuestContinuationPossible(true))
			{
				return;
			}
			if(!mem.isDead() && mem.isInRange(player, 700))
				continue;
			return;
		}
		if(!isEntryTime())
		{
			showHtmlFile(player, "" + npcId + "-NE.htm", npc, null);
			return;
		}
		showHtmlFile(player, "" + npcId + "-OK.htm", npc, null);
		entry(npcId, player);
	}
	
	private static void entry(int npcId, Player player)
	{
		Location loc = FourSepulchersSpawn._startHallSpawns.get(npcId);
		for(Player member : player.getParty().getPartyMembers())
		{
			member.teleToLocation(Location.findPointToStay(member, loc, 0, 80));
			Functions.removeItem(member, 7075, (long) 1);
			if(member.getInventory().getItemByItemId(7262) == null)
			{
				Functions.addItem(member, 7261, (long) 1);
			}
			Functions.removeItem(member, 7260, (long) 999999);
		}
		FourSepulchersSpawn._hallInUse.put(npcId, true);
	}
	
	public static void checkAnnihilated(Player player)
	{
		if(isPlayersAnnihilated())
		{
			ThreadPoolManager.getInstance().schedule(new RunnableImpl()
			{
				
				@Override
				public void runImpl() throws Exception
				{
					if(player.getParty() != null)
					{
						Player mem;
						Iterator iterator = player.getParty().getPartyMembers().iterator();
						while(iterator.hasNext() && (mem = (Player) iterator.next()).isDead())
						{
							mem.teleToLocation(169589 + Rnd.get(-80, 80), -90493 + Rnd.get(-80, 80), -2914);
						}
					}
					else
					{
						player.teleToLocation(169589 + Rnd.get(-80, 80), -90493 + Rnd.get(-80, 80), -2914);
					}
				}
			}, 5000);
		}
	}
	
	private static int minuteSelect(int min)
	{
		switch(min % 5)
		{
			case 0:
			{
				return min;
			}
			case 1:
			{
				return min - 1;
			}
			case 2:
			{
				return min - 2;
			}
			case 3:
			{
				return min + 2;
			}
		}
		return min + 1;
	}
	
	public static void managerSay(int min)
	{
		if(_inAttackTime)
		{
			if(min < 5)
			{
				return;
			}
			min = minuteSelect(min);
			String msg = "" + min + " minute(s) have passed.";
			if(min == 90)
			{
				msg = "Game over. The teleport will appear momentarily";
			}
			for(SepulcherNpcInstance npc : FourSepulchersSpawn._managers)
			{
				if(!FourSepulchersSpawn._hallInUse.get(npc.getNpcId()).booleanValue())
					continue;
				npc.sayInShout(msg);
			}
			return;
		}
		else
		{
			if(!_inEntryTime)
				return;
			String msg1 = "You may now enter the Sepulcher";
			String msg2 = "If you place your hand on the stone statue in front of each sepulcher, you will be able to enter";
			for(SepulcherNpcInstance npc : FourSepulchersSpawn._managers)
			{
				npc.sayInShout(msg1);
				npc.sayInShout(msg2);
			}
		}
	}
	
	public static FourSepulchersSpawn.GateKeeper getHallGateKeeper(int npcId)
	{
		for(FourSepulchersSpawn.GateKeeper gk : FourSepulchersSpawn._GateKeepers)
		{
			if(gk.template.npcId != npcId)
				continue;
			return gk;
		}
		return null;
	}
	
	public static void showHtmlFile(Player player, String file, NpcInstance npc, Player member)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile("SepulcherNpc/" + file);
		if(member != null)
		{
			html.replace("%member%", member.getName());
		}
		player.sendPacket(html);
	}
	
	private static boolean isPlayersAnnihilated()
	{
		for(Player pc : getPlayersInside())
		{
			if(pc.isDead())
				continue;
			return false;
		}
		return true;
	}
	
	private static List<Player> getPlayersInside()
	{
		ArrayList<Player> result = new ArrayList<>();
		for(Zone zone : getZones())
		{
			result.addAll(zone.getInsidePlayers());
		}
		return result;
	}
	
	private static boolean checkIfInZone(Creature cha)
	{
		for(Zone zone : getZones())
		{
			if(!zone.checkIfInZone(cha))
				continue;
			return true;
		}
		return false;
	}
	
	public static Zone[] getZones()
	{
		return _zone;
	}
	
	public void init()
	{
		CharListenerList.addGlobal(new OnDeathListenerImpl());
		_zone[0] = ReflectionUtils.getZone("[FourSepulchers1]");
		_zone[1] = ReflectionUtils.getZone("[FourSepulchers2]");
		_zone[2] = ReflectionUtils.getZone("[FourSepulchers3]");
		_zone[3] = ReflectionUtils.getZone("[FourSepulchers4]");
		if(_changeCoolDownTimeTask != null)
		{
			_changeCoolDownTimeTask.cancel(false);
		}
		if(_changeEntryTimeTask != null)
		{
			_changeEntryTimeTask.cancel(false);
		}
		if(_changeWarmUpTimeTask != null)
		{
			_changeWarmUpTimeTask.cancel(false);
		}
		if(_changeAttackTimeTask != null)
		{
			_changeAttackTimeTask.cancel(false);
		}
		_changeCoolDownTimeTask = null;
		_changeEntryTimeTask = null;
		_changeWarmUpTimeTask = null;
		_changeAttackTimeTask = null;
		_inEntryTime = false;
		_inAttackTime = false;
		_firstTimeRun = true;
		FourSepulchersSpawn.init();
		timeSelector();
	}
	
	@Override
	public void onLoad()
	{
		init();
	}
	
	@Override
	public void onReload()
	{
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	private static class OnDeathListenerImpl implements OnDeathListener
	{
		@Override
		public void onDeath(Creature self, Creature killer)
		{
			if(self.isPlayer() && self.getZ() >= -7250 && self.getZ() <= -6841 && checkIfInZone(self))
			{
				checkAnnihilated((Player) self);
			}
		}
	}
	
	private static class ChangeCoolDownTime extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			_inEntryTime = false;
			_inAttackTime = false;
			cleanUp();
			Calendar time = Calendar.getInstance();
			if(Calendar.getInstance().get(12) > _newCycleMin && !_firstTimeRun)
			{
				time.set(10, Calendar.getInstance().get(10) + 1);
			}
			time.set(12, _newCycleMin);
			_log.info("FourSepulchersManager: Entry time: " + time.getTime());
			if(_firstTimeRun)
			{
				_firstTimeRun = false;
			}
			long interval = time.getTimeInMillis() - System.currentTimeMillis();
			_changeEntryTimeTask = ThreadPoolManager.getInstance().schedule(new ChangeEntryTime(), interval);
			if(_changeCoolDownTimeTask != null)
			{
				_changeCoolDownTimeTask.cancel(false);
				_changeCoolDownTimeTask = null;
			}
		}
	}
	
	private static class ChangeAttackTime extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			_inEntryTime = false;
			_inAttackTime = true;
			for(FourSepulchersSpawn.GateKeeper gk : FourSepulchersSpawn._GateKeepers)
			{
				SepulcherNpcInstance npc = new SepulcherNpcInstance(IdFactory.getInstance().getNextId(), gk.template);
				npc.spawnMe(gk);
				FourSepulchersSpawn._allMobs.add(npc);
			}
			FourSepulchersSpawn.locationShadowSpawns();
			FourSepulchersSpawn.spawnMysteriousBox(31921);
			FourSepulchersSpawn.spawnMysteriousBox(31922);
			FourSepulchersSpawn.spawnMysteriousBox(31923);
			FourSepulchersSpawn.spawnMysteriousBox(31924);
			if(!_firstTimeRun)
			{
				_warmUpTimeEnd = System.currentTimeMillis();
			}
			if(_firstTimeRun)
			{
				for(double min = (double) Calendar.getInstance().get(12);min < (double) _newCycleMin;min += 1.0)
				{
					if(min % 5.0 != 0.0)
						continue;
					_log.info(Calendar.getInstance().getTime() + " Atk announce scheduled to " + min + " minute of this hour.");
					Calendar inter = Calendar.getInstance();
					inter.set(12, (int) min);
					ThreadPoolManager.getInstance().schedule(new ManagerSay(), inter.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
					break;
				}
			}
			else
			{
				ThreadPoolManager.getInstance().schedule(new ManagerSay(), 302000);
			}
			long interval = _firstTimeRun ? _attackTimeEnd - System.currentTimeMillis() : 3000000;
			_changeCoolDownTimeTask = ThreadPoolManager.getInstance().schedule(new ChangeCoolDownTime(), interval);
			if(_changeAttackTimeTask != null)
			{
				_changeAttackTimeTask.cancel(false);
				_changeAttackTimeTask = null;
			}
		}
	}
	
	private static class ChangeWarmUpTime extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			_inEntryTime = true;
			_inAttackTime = false;
			long interval = _firstTimeRun ? _warmUpTimeEnd - System.currentTimeMillis() : 120000;
			_changeAttackTimeTask = ThreadPoolManager.getInstance().schedule(new ChangeAttackTime(), interval);
			if(_changeWarmUpTimeTask != null)
			{
				_changeWarmUpTimeTask.cancel(false);
				_changeWarmUpTimeTask = null;
			}
		}
	}
	
	private static class ChangeEntryTime extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			_inEntryTime = true;
			_inAttackTime = false;
			long interval = _firstTimeRun ? _entryTimeEnd - System.currentTimeMillis() : 180000;
			ThreadPoolManager.getInstance().execute(new ManagerSay());
			_changeWarmUpTimeTask = ThreadPoolManager.getInstance().schedule(new ChangeWarmUpTime(), interval);
			if(_changeEntryTimeTask != null)
			{
				_changeEntryTimeTask.cancel(false);
				_changeEntryTimeTask = null;
			}
		}
	}
	
	private static class ManagerSay extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			if(_inAttackTime)
			{
				Calendar tmp = Calendar.getInstance();
				tmp.setTimeInMillis(System.currentTimeMillis() - _warmUpTimeEnd);
				if(tmp.get(12) + 5 < 50)
				{
					managerSay(tmp.get(12));
					ThreadPoolManager.getInstance().schedule(new ManagerSay(), 300000);
				}
				else if(tmp.get(12) + 5 >= 50)
				{
					managerSay(90);
				}
			}
			else if(_inEntryTime)
			{
				managerSay(0);
			}
		}
	}
}