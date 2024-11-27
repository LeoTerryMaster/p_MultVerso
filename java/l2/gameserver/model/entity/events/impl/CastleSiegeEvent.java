package l2.gameserver.model.entity.events.impl;

import l2.commons.collections.MultiValueSet;
import l2.commons.dao.JdbcEntityState;
import l2.commons.lang.ArrayUtils;
import l2.commons.threading.RunnableImpl;
import l2.commons.time.cron.NextTime;
import l2.commons.time.cron.SchedulingPattern;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.dao.CastleDamageZoneDAO;
import l2.gameserver.dao.CastleDoorUpgradeDAO;
import l2.gameserver.dao.CastleHiredGuardDAO;
import l2.gameserver.dao.SiegeClanDAO;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Spawner;
import l2.gameserver.model.World;
import l2.gameserver.model.Zone;
import l2.gameserver.model.base.RestartType;
import l2.gameserver.model.entity.events.objects.DoorObject;
import l2.gameserver.model.entity.events.objects.SiegeClanObject;
import l2.gameserver.model.entity.events.objects.SiegeToggleNpcObject;
import l2.gameserver.model.entity.events.objects.SpawnExObject;
import l2.gameserver.model.entity.events.objects.SpawnSimpleObject;
import l2.gameserver.model.entity.oly.HeroController;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.model.instances.residences.SiegeToggleNpcInstance;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.pledge.UnitMember;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.PlaySound;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.taskmanager.DelayedItemsManager;
import l2.gameserver.templates.item.ItemTemplate;
import l2.gameserver.templates.item.support.MerchantGuard;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.Log;
import l2.gameserver.utils.TeleportUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.napile.primitive.Containers;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.TreeIntSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.Future;

public class CastleSiegeEvent extends SiegeEvent<Castle, SiegeClanObject>
{
	public static final int MAX_SIEGE_CLANS = 20;
	public static final long DAY_IN_MILISECONDS = 86400000;
	public static final String DEFENDERS_WAITING = "defenders_waiting";
	public static final String DEFENDERS_REFUSED = "defenders_refused";
	public static final String CONTROL_TOWERS = "control_towers";
	public static final String FLAME_TOWERS = "flame_towers";
	public static final String BOUGHT_ZONES = "bought_zones";
	public static final String GUARDS = "guards";
	public static final String HIRED_GUARDS = "hired_guards";
	private final long _nextSiegeDateSetDelay;
	private IntSet _nextSiegeTimes = Containers.EMPTY_INT_SET;
	private Future<?> _nextSiegeDateSetTask;
	private boolean _firstStep;
	private NextTime[] _nextSiegeTimesPatterns = new NextTime[0];
	private Pair<ItemTemplate, Long> _onSiegeEndAttackerOwnedLeaderReward;
	private Pair<ItemTemplate, Long> _onSiegeEndDefenderOwnedLeaderReward;
	
	public CastleSiegeEvent(MultiValueSet<String> set)
	{
		super(set);
		String nextSiegeTimesPattern = set.getString("siege_schedule");
		StringTokenizer st = new StringTokenizer(nextSiegeTimesPattern, "|;");
		while(st.hasMoreTokens())
		{
			_nextSiegeTimesPatterns = ArrayUtils.add(_nextSiegeTimesPatterns, new SchedulingPattern(st.nextToken()));
		}
		String onSiegeEndAttackerOwnedLeaderReward = set.getString("on_siege_end_attacker_owned_leader_reward", null);
		if(onSiegeEndAttackerOwnedLeaderReward != null)
		{
			String[] onSiegEndAttackerOwnedLeaderRewardParts = onSiegeEndAttackerOwnedLeaderReward.split(":");
			_onSiegeEndAttackerOwnedLeaderReward = Pair.of(ItemHolder.getInstance().getTemplate(Integer.parseInt(onSiegEndAttackerOwnedLeaderRewardParts[0])), Long.valueOf(onSiegEndAttackerOwnedLeaderRewardParts[1]));
		}
		String onSiegeEndDefenderOwnedLeaderReward;
		if((onSiegeEndDefenderOwnedLeaderReward = set.getString("on_siege_end_defender_owned_leader_reward", null)) != null)
		{
			String[] onSiegeEndDefenderOwnedLeaderRewardParts = onSiegeEndDefenderOwnedLeaderReward.split(":");
			_onSiegeEndDefenderOwnedLeaderReward = Pair.of(ItemHolder.getInstance().getTemplate(Integer.parseInt(onSiegeEndDefenderOwnedLeaderRewardParts[0])), Long.valueOf(onSiegeEndDefenderOwnedLeaderRewardParts[1]));
		}
		_nextSiegeDateSetDelay = set.getLong("next_siege_date_set_delay", 86400) * 1000;
	}
	
	@Override
	public void initEvent()
	{
		super.initEvent();
		List<DoorObject> doorObjects = getObjects("doors");
		addObjects("bought_zones", CastleDamageZoneDAO.getInstance().load(getResidence()));
		for(DoorObject doorObject : doorObjects)
		{
			doorObject.setUpgradeValue(this, CastleDoorUpgradeDAO.getInstance().load(doorObject.getUId()));
			doorObject.getDoor().addListener(_doorDeathListener);
		}
	}
	
	@Override
	public void processStep(Clan newOwnerClan)
	{
		Clan oldOwnerClan = getResidence().getOwner();
		getResidence().changeOwner(newOwnerClan);
		if(oldOwnerClan != null)
		{
			SiegeClanObject ownerSiegeClan = getSiegeClan("defenders", oldOwnerClan);
			removeObject("defenders", ownerSiegeClan);
			ownerSiegeClan.setType("attackers");
			addObject("attackers", ownerSiegeClan);
		}
		else
		{
			if(getObjects("attackers").size() == 1)
			{
				stopEvent();
				return;
			}
			int allianceObjectId = newOwnerClan.getAllyId();
			if(allianceObjectId > 0)
			{
				List<SiegeClanObject> attackers = getObjects("attackers");
				boolean sameAlliance = true;
				for(SiegeClanObject sc : attackers)
				{
					if(sc == null || sc.getClan().getAllyId() == allianceObjectId)
						continue;
					sameAlliance = false;
				}
				if(sameAlliance)
				{
					stopEvent();
					return;
				}
			}
		}
		SiegeClanObject newOwnerSiegeClan = getSiegeClan("attackers", newOwnerClan);
		newOwnerSiegeClan.deleteFlag();
		newOwnerSiegeClan.setType("defenders");
		removeObject("attackers", newOwnerSiegeClan);
		List<SiegeClanObject> defenders = removeObjects("defenders");
		for(SiegeClanObject siegeClan : defenders)
		{
			siegeClan.setType("attackers");
		}
		addObject("defenders", newOwnerSiegeClan);
		addObjects("attackers", defenders);
		updateParticles(true, "attackers", "defenders");
		teleportPlayers("attackers");
		teleportPlayers("spectators");
		if(!_firstStep)
		{
			_firstStep = true;
			broadcastTo(SystemMsg.THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_HAS_BEEN_DISSOLVED, "attackers", "defenders");
			if(_oldOwner != null)
			{
				spawnAction("hired_guards", false);
				damageZoneAction(false);
				removeObjects("hired_guards");
				removeObjects("bought_zones");
				CastleDamageZoneDAO.getInstance().delete(getResidence());
			}
			else
			{
				spawnAction("guards", false);
			}
			List<DoorObject> doorObjects = getObjects("doors");
			for(DoorObject doorObject : doorObjects)
			{
				doorObject.setWeak(true);
				doorObject.setUpgradeValue(this, 0);
				CastleDoorUpgradeDAO.getInstance().delete(doorObject.getUId());
			}
		}
		spawnAction("doors", true);
		spawnAction("control_towers", true);
		spawnAction("flame_towers", true);
		despawnSiegeSummons();
	}
	
	@Override
	public void startEvent()
	{
		_oldOwner = getResidence().getOwner();
		if(_oldOwner != null)
		{
			addObject("defenders", new SiegeClanObject("defenders", _oldOwner, 0));
			if(getResidence().getSpawnMerchantTickets().size() > 0)
			{
				for(ItemInstance item : getResidence().getSpawnMerchantTickets())
				{
					MerchantGuard guard = getResidence().getMerchantGuard(item.getItemId());
					addObject("hired_guards", new SpawnSimpleObject(guard.getNpcId(), item.getLoc()));
					item.deleteMe();
				}
				CastleHiredGuardDAO.getInstance().delete(getResidence());
				spawnAction("hired_guards", true);
			}
		}
		List attackers = getObjects("attackers");
		if(attackers.isEmpty())
		{
			if(_oldOwner == null)
			{
				broadcastToWorld(new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST).addResidenceName(getResidence()));
			}
			else
			{
				broadcastToWorld(new SystemMessage2(SystemMsg.S1S_SIEGE_WAS_CANCELED_BECAUSE_THERE_WERE_NO_CLANS_THAT_PARTICIPATED).addResidenceName(getResidence()));
			}
			reCalcNextTime(false);
			return;
		}
		SiegeClanDAO.getInstance().delete(getResidence());
		updateParticles(true, "attackers", "defenders");
		broadcastTo(SystemMsg.THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_IS_IN_EFFECT, "attackers");
		super.startEvent();
		if(_oldOwner == null)
		{
			initControlTowers();
		}
		else
		{
			damageZoneAction(true);
		}
	}
	
	@Override
	public void stopEvent(boolean step)
	{
		List<DoorObject> doorObjects = getObjects("doors");
		for(DoorObject doorObject : doorObjects)
		{
			doorObject.setWeak(false);
		}
		damageZoneAction(false);
		updateParticles(false, "attackers", "defenders");
		List<SiegeClanObject> attackers = removeObjects("attackers");
		for(SiegeClanObject siegeClan : attackers)
		{
			siegeClan.deleteFlag();
		}
		broadcastToWorld(new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_IS_FINISHED).addResidenceName(getResidence()));
		removeObjects("defenders");
		removeObjects("defenders_waiting");
		removeObjects("defenders_refused");
		Clan ownerClan = getResidence().getOwner();
		if(ownerClan != null)
		{
			UnitMember leader;
			if(_oldOwner == ownerClan)
			{
				ownerClan.broadcastToOnlineMembers(new SystemMessage2(SystemMsg.SINCE_YOUR_CLAN_EMERGED_VICTORIOUS_FROM_THE_SIEGE_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLANS_REPUTATION_SCORE).addInteger(ownerClan.incReputation(1000, false, toString())));
				leader = ownerClan.getLeader();
				if(leader != null && _onSiegeEndDefenderOwnedLeaderReward != null)
				{
					int rewardItemId = _onSiegeEndDefenderOwnedLeaderReward.getLeft().getItemId();
					long rewardItemCount = _onSiegeEndDefenderOwnedLeaderReward.getRight();
					Player leaderPlayer = World.getPlayer(leader.getObjectId());
					if(leaderPlayer != null && leaderPlayer.isOnline())
					{
						ItemFunctions.addItem(leaderPlayer, rewardItemId, rewardItemCount, true);
						Log.LogItem(leaderPlayer, Log.ItemLog.PostSend, rewardItemId, rewardItemCount);
					}
					else
					{
						DelayedItemsManager.getInstance().addDelayed(leader.getObjectId(), rewardItemId, (int) rewardItemCount, 0, "End siege owner leader reward item " + rewardItemId + "(" + rewardItemCount + ")");
					}
				}
			}
			else
			{
				broadcastToWorld(new SystemMessage2(SystemMsg.CLAN_S1_IS_VICTORIOUS_OVER_S2S_CASTLE_SIEGE).addString(ownerClan.getName()).addResidenceName(getResidence()));
				ownerClan.broadcastToOnlineMembers(new SystemMessage2(SystemMsg.SINCE_YOUR_CLAN_EMERGED_VICTORIOUS_FROM_THE_SIEGE_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLANS_REPUTATION_SCORE).addInteger(ownerClan.incReputation(1500, false, toString())));
				if(_oldOwner != null)
				{
					_oldOwner.broadcastToOnlineMembers(new SystemMessage2(SystemMsg.YOUR_CLAN_HAS_FAILED_TO_DEFEND_THE_CASTLE_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOU_CLAN_REPUTATION_SCORE_AND_ADDED_TO_YOUR_OPPONENTS).addInteger(-_oldOwner.incReputation(-1500, false, toString())));
				}
				leader = ownerClan.getLeader();
				for(UnitMember member : ownerClan)
				{
					Player player = member.getPlayer();
					if(player == null)
						continue;
					player.sendPacket(PlaySound.SIEGE_VICTORY);
					if(!player.isOnline() || !player.isNoble())
						continue;
					HeroController.getInstance().addHeroDiary(player.getObjectId(), 3, getResidence().getId());
				}
				if(_onSiegeEndAttackerOwnedLeaderReward != null)
				{
					int rewardItemId = _onSiegeEndAttackerOwnedLeaderReward.getLeft().getItemId();
					long rewardItemCount = _onSiegeEndAttackerOwnedLeaderReward.getRight();
					Player leaderPlayer = World.getPlayer(leader.getObjectId());
					if(leaderPlayer != null && leaderPlayer.isOnline())
					{
						ItemFunctions.addItem(leaderPlayer, rewardItemId, rewardItemCount, true);
						Log.LogItem(leaderPlayer, Log.ItemLog.PostSend, rewardItemId, rewardItemCount);
					}
					else
					{
						DelayedItemsManager.getInstance().addDelayed(leader.getObjectId(), rewardItemId, (int) rewardItemCount, 0, "End siege owner leader reward item " + rewardItemId + "(" + rewardItemCount + ")");
					}
				}
			}
			getResidence().getOwnDate().setTimeInMillis(System.currentTimeMillis());
			getResidence().getLastSiegeDate().setTimeInMillis(getResidence().getSiegeDate().getTimeInMillis());
		}
		else
		{
			broadcastToWorld(new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW).addResidenceName(getResidence()));
			getResidence().getOwnDate().setTimeInMillis(0);
			getResidence().getLastSiegeDate().setTimeInMillis(0);
		}
		despawnSiegeSummons();
		if(_oldOwner != null)
		{
			spawnAction("hired_guards", false);
			removeObjects("hired_guards");
		}
		super.stopEvent(step);
	}
	
	@Override
	public void reCalcNextTime(boolean onInit)
	{
		clearActions();
		long currentTimeMillis = System.currentTimeMillis();
		Calendar startSiegeDate = getResidence().getSiegeDate();
		Calendar ownSiegeDate = getResidence().getOwnDate();
		if(onInit)
		{
			if(startSiegeDate.getTimeInMillis() > currentTimeMillis)
			{
				registerActions();
			}
			else if(startSiegeDate.getTimeInMillis() == 0)
			{
				if(currentTimeMillis - ownSiegeDate.getTimeInMillis() > _nextSiegeDateSetDelay)
				{
					setNextSiegeTime();
				}
				else
				{
					generateNextSiegeDates();
				}
			}
			else if(startSiegeDate.getTimeInMillis() <= currentTimeMillis)
			{
				setNextSiegeTime();
			}
		}
		else if(getResidence().getOwner() != null)
		{
			getResidence().getSiegeDate().setTimeInMillis(0);
			getResidence().setJdbcState(JdbcEntityState.UPDATED);
			getResidence().update();
			generateNextSiegeDates();
		}
		else
		{
			setNextSiegeTime();
		}
	}
	
	@Override
	public void loadSiegeClans()
	{
		super.loadSiegeClans();
		addObjects("defenders_waiting", SiegeClanDAO.getInstance().load(getResidence(), "defenders_waiting"));
		addObjects("defenders_refused", SiegeClanDAO.getInstance().load(getResidence(), "defenders_refused"));
	}
	
	@Override
	public void setRegistrationOver(boolean b)
	{
		if(b)
		{
			broadcastToWorld(new SystemMessage2(SystemMsg.THE_DEADLINE_TO_REGISTER_FOR_THE_SIEGE_OF_S1_HAS_PASSED).addResidenceName(getResidence()));
		}
		super.setRegistrationOver(b);
	}
	
	@Override
	public void announce(int val)
	{
		int min = val / 60;
		int hour = min / 60;
		SystemMessage2 msg = hour > 0 ? new SystemMessage2(SystemMsg.S1_HOURS_UNTIL_CASTLE_SIEGE_CONCLUSION).addInteger(hour) : min > 0 ? new SystemMessage2(SystemMsg.S1_MINUTES_UNTIL_CASTLE_SIEGE_CONCLUSION).addInteger(min) : new SystemMessage2(SystemMsg.THIS_CASTLE_SIEGE_WILL_END_IN_S1_SECONDS).addInteger(val);
		broadcastTo(msg, "attackers", "defenders");
	}
	
	private void initControlTowers()
	{
		List<SpawnExObject> objects = getObjects("guards");
		ArrayList<Spawner> spawns = new ArrayList<>();
		for(SpawnExObject o : objects)
		{
			spawns.addAll(o.getSpawns());
		}
		List<SiegeToggleNpcObject> ct = getObjects("control_towers");
		for(Spawner spawn : spawns)
		{
			Location spawnLoc = spawn.getCurrentSpawnRange().getRandomLoc(ReflectionManager.DEFAULT.getGeoIndex());
			SiegeToggleNpcInstance closestCt = null;
			double distanceClosest = 0.0;
			for(SiegeToggleNpcObject c : ct)
			{
				SiegeToggleNpcInstance npcTower = c.getToggleNpc();
				double distance = npcTower.getDistance(spawnLoc);
				if(closestCt == null || distance < distanceClosest)
				{
					closestCt = npcTower;
					distanceClosest = distance;
				}
				closestCt.register(spawn);
			}
		}
	}
	
	private void damageZoneAction(boolean active)
	{
		zoneAction("bought_zones", active);
	}
	
	private void setNextSiegeTime()
	{
		Calendar calendar = (Calendar) Config.CASTLE_VALIDATION_DATE.clone();
		calendar.set(7, 1);
		calendar.set(11, getResidence().getLastSiegeDate().get(11));
		if(calendar.before(Config.CASTLE_VALIDATION_DATE))
		{
			calendar.add(3, 1);
		}
		calendar.setTimeInMillis(scheduleNextTime(calendar.getTimeInMillis(), _nextSiegeTimesPatterns[0]));
		setNextSiegeTime(calendar.getTimeInMillis());
	}
	
	public void generateNextSiegeDates()
	{
		if(getResidence().getSiegeDate().getTimeInMillis() != 0)
		{
			return;
		}
		Calendar calendar = (Calendar) Config.CASTLE_VALIDATION_DATE.clone();
		calendar.set(7, 1);
		if(calendar.before(Config.CASTLE_VALIDATION_DATE))
		{
			calendar.add(3, 1);
		}
		_nextSiegeTimes = new TreeIntSet();
		for(NextTime nextTime : _nextSiegeTimesPatterns)
		{
			_nextSiegeTimes.add((int) (scheduleNextTime(calendar.getTimeInMillis(), nextTime) / 1000));
		}
		long diff = getResidence().getOwnDate().getTimeInMillis() + _nextSiegeDateSetDelay - System.currentTimeMillis();
		_nextSiegeDateSetTask = ThreadPoolManager.getInstance().schedule(new NextSiegeDateSet(), diff);
	}
	
	protected long scheduleNextTime(long baseMs, NextTime nextTime)
	{
		GregorianCalendar cal = new GregorianCalendar(TimeZone.getDefault());
		cal.setTimeInMillis(baseMs);
		cal.set(13, 0);
		cal.set(14, 0);
		long nextTimeMs = cal.getTimeInMillis();
		while(nextTimeMs < System.currentTimeMillis())
		{
			nextTimeMs = nextTime.next(nextTimeMs);
		}
		return nextTimeMs;
	}
	
	public void setNextSiegeTime(int id)
	{
		if(!_nextSiegeTimes.contains(id) || _nextSiegeDateSetTask == null)
		{
			return;
		}
		_nextSiegeTimes = Containers.EMPTY_INT_SET;
		_nextSiegeDateSetTask.cancel(false);
		_nextSiegeDateSetTask = null;
		setNextSiegeTime((long) id * 1000);
	}
	
	private void setNextSiegeTime(long g)
	{
		broadcastToWorld(new SystemMessage2(SystemMsg.S1_HAS_ANNOUNCED_THE_NEXT_CASTLE_SIEGE_TIME).addResidenceName(getResidence()));
		clearActions();
		getResidence().getSiegeDate().setTimeInMillis(g);
		getResidence().setJdbcState(JdbcEntityState.UPDATED);
		getResidence().update();
		registerActions();
	}
	
	@Override
	public boolean isAttackersInAlly()
	{
		return !_firstStep;
	}
	
	public int[] getNextSiegeTimes()
	{
		return _nextSiegeTimes.toArray();
	}
	
	@Override
	public boolean canResurrect(Player resurrectPlayer, Creature target, boolean force)
	{
		boolean playerInZone = resurrectPlayer.isInZone(Zone.ZoneType.SIEGE);
		boolean targetInZone = target.isInZone(Zone.ZoneType.SIEGE);
		if(!playerInZone && !targetInZone)
		{
			return true;
		}
		if(!targetInZone)
		{
			return false;
		}
		Player targetPlayer = target.getPlayer();
		CastleSiegeEvent siegeEvent = target.getEvent(CastleSiegeEvent.class);
		if(siegeEvent != this)
		{
			if(force)
			{
				targetPlayer.sendPacket(SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE);
			}
			resurrectPlayer.sendPacket(force ? SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE : SystemMsg.INVALID_TARGET);
			return false;
		}
		SiegeClanObject targetSiegeClan = siegeEvent.getSiegeClan("attackers", targetPlayer.getClan());
		if(targetSiegeClan == null)
		{
			targetSiegeClan = siegeEvent.getSiegeClan("defenders", targetPlayer.getClan());
		}
		if(targetSiegeClan.getType() == "attackers")
		{
			if(targetSiegeClan.getFlag() == null)
			{
				if(force)
				{
					targetPlayer.sendPacket(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
				}
				resurrectPlayer.sendPacket(force ? SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE : SystemMsg.INVALID_TARGET);
				return false;
			}
		}
		else
		{
			List<SiegeToggleNpcObject> towers = getObjects("control_towers");
			int deadTowerCnt = 0;
			for(SiegeToggleNpcObject t : towers)
			{
				if(t.isAlive())
					continue;
				++deadTowerCnt;
			}
			if(deadTowerCnt > 1)
			{
				if(force)
				{
					targetPlayer.sendPacket(SystemMsg.THE_GUARDIAN_TOWER_HAS_BEEN_DESTROYED_AND_RESURRECTION_IS_NOT_POSSIBLE);
				}
				resurrectPlayer.sendPacket(force ? SystemMsg.THE_GUARDIAN_TOWER_HAS_BEEN_DESTROYED_AND_RESURRECTION_IS_NOT_POSSIBLE : SystemMsg.INVALID_TARGET);
				return false;
			}
		}
		return true;
	}
	
	@Override
	public Location getRestartLoc(Player player, RestartType type)
	{
		Location loc;
		switch(type)
		{
			case TO_VILLAGE:
			{
				loc = TeleportUtils.getRestartLocation(player, RestartType.TO_VILLAGE);
				break;
			}
			default:
			{
				loc = super.getRestartLoc(player, type);
			}
		}
		return loc;
	}
	
	private class NextSiegeDateSet extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			setNextSiegeTime();
		}
	}
}