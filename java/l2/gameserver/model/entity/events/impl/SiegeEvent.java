package l2.gameserver.model.entity.events.impl;

import l2.commons.collections.LazyArrayList;
import l2.commons.collections.MultiValueSet;
import l2.commons.dao.JdbcEntityState;
import l2.commons.lang.reference.HardReference;
import l2.gameserver.Config;
import l2.gameserver.dao.SiegeClanDAO;
import l2.gameserver.data.xml.holder.ResidenceHolder;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.listener.actor.OnDeathListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.base.RestartType;
import l2.gameserver.model.entity.events.GlobalEvent;
import l2.gameserver.model.entity.events.objects.SiegeClanObject;
import l2.gameserver.model.entity.events.objects.ZoneObject;
import l2.gameserver.model.entity.residence.Residence;
import l2.gameserver.model.instances.DoorInstance;
import l2.gameserver.model.instances.SummonInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.components.IStaticPacket;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;
import l2.gameserver.tables.ClanTable;
import l2.gameserver.templates.DoorTemplate;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public abstract class SiegeEvent<R extends Residence, S extends SiegeClanObject> extends GlobalEvent
{
	public static final String OWNER = "owner";
	public static final String OLD_OWNER = "old_owner";
	public static final String ATTACKERS = "attackers";
	public static final String DEFENDERS = "defenders";
	public static final String SPECTATORS = "spectators";
	public static final String SIEGE_ZONES = "siege_zones";
	public static final String FLAG_ZONES = "flag_zones";
	public static final String DAY_OF_WEEK = "day_of_week";
	public static final String HOUR_OF_DAY = "hour_of_day";
	public static final String REGISTRATION = "registration";
	public static final String DOORS = "doors";
	protected R _residence;
	protected int _dayOfWeek;
	protected int _hourOfDay;
	protected Clan _oldOwner;
	protected OnDeathListener _doorDeathListener;
	protected List<HardReference<SummonInstance>> _siegeSummons;
	private boolean _isInProgress;
	private boolean _isRegistrationOver;
	
	public SiegeEvent(MultiValueSet<String> set)
	{
		super(set);
		_doorDeathListener = new DoorDeathListener();
		_siegeSummons = new ArrayList<>();
		_dayOfWeek = set.getInteger("day_of_week", 0);
		_hourOfDay = set.getInteger("hour_of_day", 0);
	}
	
	@Override
	public void startEvent()
	{
		setInProgress(true);
		super.startEvent();
	}
	
	@Override
	public final void stopEvent()
	{
		stopEvent(false);
	}
	
	public void stopEvent(boolean step)
	{
		despawnSiegeSummons();
		setInProgress(false);
		reCalcNextTime(false);
		super.stopEvent();
	}
	
	public void processStep(Clan clan)
	{
	}
	
	@Override
	public void reCalcNextTime(boolean onInit)
	{
		clearActions();
		Calendar startSiegeDate = getResidence().getSiegeDate();
		if(onInit)
		{
			if(startSiegeDate.getTimeInMillis() <= System.currentTimeMillis())
			{
				startSiegeDate.set(7, _dayOfWeek);
				startSiegeDate.set(11, _hourOfDay);
				validateSiegeDate(startSiegeDate, 2);
				getResidence().setJdbcState(JdbcEntityState.UPDATED);
			}
		}
		else
		{
			startSiegeDate.add(3, 2);
			getResidence().setJdbcState(JdbcEntityState.UPDATED);
		}
		registerActions();
		getResidence().update();
	}
	
	protected void validateSiegeDate(Calendar calendar, int add)
	{
		calendar.set(12, 0);
		calendar.set(13, 0);
		calendar.set(14, 0);
		while(calendar.getTimeInMillis() < System.currentTimeMillis())
		{
			calendar.add(3, add);
		}
	}
	
	@Override
	protected long startTimeMillis()
	{
		return getResidence().getSiegeDate().getTimeInMillis();
	}
	
	@Override
	public void teleportPlayers(String t)
	{
		S siegeClan;
		List<Player> players = new ArrayList();
		Clan ownerClan = getResidence().getOwner();
		if(t.equalsIgnoreCase("owner"))
		{
			if(ownerClan != null)
			{
				for(Player player : getPlayersInZone())
				{
					if(player.getClan() != ownerClan)
						continue;
					players.add(player);
				}
			}
		}
		else if(t.equalsIgnoreCase("attackers"))
		{
			for(Player player : getPlayersInZone())
			{
				siegeClan = getSiegeClan("attackers", player.getClan());
				if(siegeClan == null || !siegeClan.isParticle(player))
					continue;
				players.add(player);
			}
		}
		else if(t.equalsIgnoreCase("defenders"))
		{
			for(Player player : getPlayersInZone())
			{
				if(ownerClan != null && player.getClan() != null && player.getClan() == ownerClan || (siegeClan = getSiegeClan("defenders", player.getClan())) == null || !siegeClan.isParticle(player))
					continue;
				players.add(player);
			}
		}
		else if(t.equalsIgnoreCase("spectators"))
		{
			for(Player player : getPlayersInZone())
			{
				if(ownerClan != null && player.getClan() != null && player.getClan() == ownerClan || player.getClan() != null && (getSiegeClan("attackers", player.getClan()) != null || getSiegeClan("defenders", player.getClan()) != null))
					continue;
				players.add(player);
			}
		}
		else
		{
			players = getPlayersInZone();
		}
		for(Player player : players)
		{
			Location loc = t.equalsIgnoreCase("owner") || t.equalsIgnoreCase("defenders") ? getResidence().getOwnerRestartPoint() : getResidence().getNotOwnerRestartPoint(player);
			player.teleToLocation(loc, ReflectionManager.DEFAULT);
		}
	}
	
	public List<Player> getPlayersInZone()
	{
		List<ZoneObject> zones = getObjects("siege_zones");
		LazyArrayList<Player> result = new LazyArrayList<>();
		for(ZoneObject zone : zones)
		{
			result.addAll(zone.getInsidePlayers());
		}
		return result;
	}
	
	public void broadcastInZone(L2GameServerPacket... packet)
	{
		for(Player player : getPlayersInZone())
		{
			player.sendPacket(packet);
		}
	}
	
	public void broadcastInZone(IStaticPacket... packet)
	{
		for(Player player : getPlayersInZone())
		{
			player.sendPacket(packet);
		}
	}
	
	public boolean checkIfInZone(Creature character)
	{
		List<ZoneObject> zones = getObjects("siege_zones");
		for(ZoneObject zone : zones)
		{
			if(!zone.checkIfInZone(character))
				continue;
			return true;
		}
		return false;
	}
	
	public void broadcastInZone2(IStaticPacket... packet)
	{
		for(Player player : getResidence().getZone().getInsidePlayers())
		{
			player.sendPacket(packet);
		}
	}
	
	public void broadcastInZone2(L2GameServerPacket... packet)
	{
		for(Player player : getResidence().getZone().getInsidePlayers())
		{
			player.sendPacket(packet);
		}
	}
	
	public void loadSiegeClans()
	{
		addObjects("attackers", SiegeClanDAO.getInstance().load(getResidence(), "attackers"));
		addObjects("defenders", SiegeClanDAO.getInstance().load(getResidence(), "defenders"));
	}
	
	public S newSiegeClan(String type, int clanId, long param, long date)
	{
		Clan clan = ClanTable.getInstance().getClan(clanId);
		return (S) (clan == null ? null : new SiegeClanObject(type, clan, param, date));
	}
	
	public void updateParticles(boolean start, String... arg)
	{
		for(String a : arg)
		{
			List<SiegeClanObject> siegeClans = getObjects(a);
			for(SiegeClanObject s : siegeClans)
			{
				s.setEvent(start, this);
			}
		}
	}
	
	public S getSiegeClan(String name, Clan clan)
	{
		if(clan == null)
		{
			return null;
		}
		return getSiegeClan(name, clan.getClanId());
	}
	
	public S getSiegeClan(String name, int objectId)
	{
		List siegeClanList = getObjects(name);
		if(siegeClanList.isEmpty())
		{
			return null;
		}
		for(int i = 0;i < siegeClanList.size();++i)
		{
			SiegeClanObject siegeClan = (SiegeClanObject) siegeClanList.get(i);
			if(siegeClan.getObjectId() != objectId)
				continue;
			return (S) siegeClan;
		}
		return null;
	}
	
	public void broadcastTo(IStaticPacket packet, String... types)
	{
		for(String type : types)
		{
			List<SiegeClanObject> siegeClans = getObjects(type);
			for(SiegeClanObject siegeClan : siegeClans)
			{
				siegeClan.broadcast(packet);
			}
		}
	}
	
	public void broadcastTo(L2GameServerPacket packet, String... types)
	{
		for(String type : types)
		{
			List<SiegeClanObject> siegeClans = getObjects(type);
			for(SiegeClanObject siegeClan : siegeClans)
			{
				siegeClan.broadcast(packet);
			}
		}
	}
	
	@Override
	public void initEvent()
	{
		_residence = ResidenceHolder.getInstance().getResidence(getId());
		loadSiegeClans();
		clearActions();
		super.initEvent();
	}
	
	@Override
	protected void printInfo()
	{
		long startSiegeMillis = startTimeMillis();
		if(startSiegeMillis == 0)
		{
			info(getName() + " time - undefined");
		}
		else
		{
			info(getName() + " time - " + TimeUtils.toSimpleFormat(startSiegeMillis));
		}
	}
	
	@Override
	public boolean ifVar(String name)
	{
		if(name.equals("owner"))
		{
			return getResidence().getOwner() != null;
		}
		if(name.equals("old_owner"))
		{
			return _oldOwner != null;
		}
		return false;
	}
	
	@Override
	public boolean isParticle(Player player)
	{
		if(!isInProgress() || player.getClan() == null)
		{
			return false;
		}
		return getSiegeClan("attackers", player.getClan()) != null || getSiegeClan("defenders", player.getClan()) != null;
	}
	
	@Override
	public void checkRestartLocs(Player player, Map<RestartType, Boolean> r)
	{
		if(getObjects("flag_zones").isEmpty())
		{
			return;
		}
		S clan = getSiegeClan("attackers", player.getClan());
		if(clan != null && clan.getFlag() != null)
		{
			r.put(RestartType.TO_FLAG, Boolean.TRUE);
		}
	}
	
	@Override
	public Location getRestartLoc(Player player, RestartType type)
	{
		S attackerClan = getSiegeClan("attackers", player.getClan());
		Location loc = null;
		switch(type)
		{
			case TO_FLAG:
			{
				if(!getObjects("flag_zones").isEmpty() && attackerClan != null && attackerClan.getFlag() != null)
				{
					loc = Location.findPointToStay(attackerClan.getFlag(), 50, 75);
					break;
				}
				player.sendPacket(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
			}
		}
		return loc;
	}
	
	@Override
	public int getRelation(Player thisPlayer, Player targetPlayer, int result)
	{
		Clan clan1 = thisPlayer.getClan();
		Clan clan2 = targetPlayer.getClan();
		if(clan1 == null || clan2 == null)
		{
			return result;
		}
		SiegeEvent siegeEvent2 = targetPlayer.getEvent(SiegeEvent.class);
		if(this == siegeEvent2)
		{
			result |= 512;
			S siegeClan1 = getSiegeClan("attackers", clan1);
			S siegeClan2 = getSiegeClan("attackers", clan2);
			if((siegeClan1 != null || siegeClan2 != null) && (siegeClan1 == null || siegeClan2 == null || siegeClan1 != siegeClan2 && !isAttackersInAlly()))
			{
				result |= 4096;
			}
			else
			{
				result |= 2048;
			}
			
			if(siegeClan1 != null)
			{
				result |= 1024;
			}
		}
		return result;
	}
	
	@Override
	public int getUserRelation(Player thisPlayer, int oldRelation)
	{
		S siegeClan = getSiegeClan("attackers", thisPlayer.getClan());
		if(siegeClan != null)
			return oldRelation | 384;
		else
			return oldRelation | 128;
	}
	
	@Override
	public SystemMsg checkForAttack(Creature target, Creature attacker, Skill skill, boolean force)
	{
		SiegeEvent siegeEvent = target.getEvent(SiegeEvent.class);
		if(this != siegeEvent)
		{
			return null;
		}
		if(!checkIfInZone(target) || !checkIfInZone(attacker))
		{
			return null;
		}
		Player player = target.getPlayer();
		if(player == null)
		{
			return null;
		}
		S siegeClan1 = getSiegeClan("attackers", player.getClan());
		if(siegeClan1 == null && attacker.isSiegeGuard())
		{
			return SystemMsg.INVALID_TARGET;
		}
		Player playerAttacker = attacker.getPlayer();
		if(playerAttacker == null)
		{
			return SystemMsg.INVALID_TARGET;
		}
		S siegeClan2 = getSiegeClan("attackers", playerAttacker.getClan());
		if(Config.ALLOW_TEMPORARILY_ALLY_ON_FIRST_SIEGE && (siegeClan1 == null && siegeClan2 == null || siegeClan1 != null && siegeClan2 != null && (siegeClan1 == siegeClan2 || isAttackersInAlly())))
		{
			return SystemMsg.INVALID_TARGET;
		}
		if(siegeClan1 == null && siegeClan2 == null)
		{
			return SystemMsg.INVALID_TARGET;
		}
		return null;
	}
	
	@Override
	public boolean isInProgress()
	{
		return _isInProgress;
	}
	
	public void setInProgress(boolean b)
	{
		_isInProgress = b;
	}
	
	@Override
	public void action(String name, boolean start)
	{
		if(name.equalsIgnoreCase("registration"))
		{
			setRegistrationOver(!start);
		}
		else
		{
			super.action(name, start);
		}
	}
	
	public boolean isAttackersInAlly()
	{
		return false;
	}
	
	@Override
	public List<Player> broadcastPlayers(int range)
	{
		return itemObtainPlayers();
	}
	
	@Override
	public List<Player> itemObtainPlayers()
	{
		List<Player> playersInZone = getPlayersInZone();
		LazyArrayList<Player> list = new LazyArrayList<>(playersInZone.size());
		for(Player player : getPlayersInZone())
		{
			if(player.getEvent(getClass()) != this)
				continue;
			list.add(player);
		}
		return list;
	}
	
	public Location getEnterLoc(Player player)
	{
		S siegeClan = getSiegeClan("attackers", player.getClan());
		if(siegeClan != null)
		{
			if(siegeClan.getFlag() != null)
			{
				return Location.findAroundPosition(siegeClan.getFlag(), 50, 75);
			}
			return getResidence().getNotOwnerRestartPoint(player);
		}
		return getResidence().getOwnerRestartPoint();
	}
	
	public R getResidence()
	{
		return _residence;
	}
	
	public boolean isRegistrationOver()
	{
		return _isRegistrationOver;
	}
	
	public void setRegistrationOver(boolean b)
	{
		_isRegistrationOver = b;
	}
	
	public void addSiegeSummon(SummonInstance summon)
	{
		_siegeSummons.add(summon.getRef());
	}
	
	public boolean containsSiegeSummon(SummonInstance cha)
	{
		return _siegeSummons.contains(cha.getRef());
	}
	
	public void despawnSiegeSummons()
	{
		for(HardReference<SummonInstance> ref : _siegeSummons)
		{
			SummonInstance summon = ref.get();
			if(summon == null)
				continue;
			summon.unSummon();
		}
		_siegeSummons.clear();
	}
	
	public class DoorDeathListener implements OnDeathListener
	{
		@Override
		public void onDeath(Creature actor, Creature killer)
		{
			if(!isInProgress())
			{
				return;
			}
			DoorInstance door = (DoorInstance) actor;
			if(door.getDoorType() == DoorTemplate.DoorType.WALL)
			{
				return;
			}
			broadcastTo(SystemMsg.THE_CASTLE_GATE_HAS_BEEN_DESTROYED, "attackers", "defenders");
		}
	}
}