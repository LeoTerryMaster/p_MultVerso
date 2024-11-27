package l2.gameserver.model.entity.events.impl;

import l2.commons.collections.JoinedIterator;
import l2.commons.collections.MultiValueSet;
import l2.gameserver.listener.actor.player.OnPlayerExitListener;
import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.Zone;
import l2.gameserver.model.base.TeamType;
import l2.gameserver.model.entity.events.GlobalEvent;
import l2.gameserver.model.entity.events.objects.DuelSnapshotObject;
import l2.gameserver.network.l2.components.IStaticPacket;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.ExDuelStart;
import l2.gameserver.network.l2.s2c.ExDuelUpdateUserInfo;
import l2.gameserver.network.l2.s2c.PlaySound;
import l2.gameserver.network.l2.s2c.SystemMessage2;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public abstract class DuelEvent extends GlobalEvent implements Iterable<DuelSnapshotObject>
{
	public static final String RED_TEAM = TeamType.RED.name();
	public static final String BLUE_TEAM = TeamType.BLUE.name();
	protected final AtomicReference<DuelState> _duelState;
	protected OnPlayerExitListener _playerExitListener;
	protected TeamType _winner;
	protected boolean _aborted;
	
	public DuelEvent(MultiValueSet<String> set)
	{
		super(set);
		_playerExitListener = new OnPlayerExitListenerImpl();
		_winner = TeamType.NONE;
		_duelState = new AtomicReference<>(DuelState.EPrepare);
	}
	
	protected DuelEvent(int id, String name)
	{
		super(id, name);
		_playerExitListener = new OnPlayerExitListenerImpl();
		_winner = TeamType.NONE;
		_duelState = new AtomicReference<>(DuelState.EPrepare);
	}
	
	@Override
	public void initEvent()
	{
	}
	
	public abstract boolean canDuel(Player player, Player target, boolean first);
	
	public abstract void askDuel(Player player, Player target);
	
	public abstract void createDuel(Player player, Player target);
	
	public abstract void playerExit(Player player);
	
	public abstract void packetSurrender(Player player);
	
	public abstract void onDie(Player player);
	
	public abstract int getDuelType();
	
	private boolean canStart()
	{
		if(_duelState.get() != DuelState.EPrepare)
		{
			return false;
		}
		for(DuelSnapshotObject dso : this)
		{
			Player player = dso.getPlayer();
			if(player == null)
			{
				return false;
			}
			IStaticPacket pkt = checkPlayer(player);
			if(pkt == null)
				continue;
			sendPacket(pkt);
			abortDuel(player);
			return false;
		}
		return true;
	}
	
	@Override
	public void action(String name, boolean start)
	{
		if(name.equalsIgnoreCase("event"))
		{
			if(start)
			{
				if(canStart())
				{
					startEvent();
				}
			}
			else
			{
				stopEvent();
			}
		}
	}
	
	@Override
	public void startEvent()
	{
		if(_duelState.compareAndSet(DuelState.EPrepare, DuelState.EInProgress))
		{
			updatePlayers(true, false);
			sendPackets(new ExDuelStart(this), PlaySound.B04_S01, SystemMsg.LET_THE_DUEL_BEGIN);
			for(DuelSnapshotObject snapshot : this)
			{
				sendPacket(new ExDuelUpdateUserInfo(snapshot.getPlayer()), snapshot.getTeam().revert().name());
			}
		}
	}
	
	public void sendPacket(IStaticPacket packet, String... ar)
	{
		for(String a : ar)
		{
			List<DuelSnapshotObject> objs = getObjects(a);
			for(DuelSnapshotObject obj : objs)
			{
				obj.getPlayer().sendPacket(packet);
			}
		}
	}
	
	public void sendPacket(IStaticPacket packet)
	{
		sendPackets(packet);
	}
	
	public void sendPackets(IStaticPacket... packet)
	{
		for(DuelSnapshotObject d : this)
		{
			d.getPlayer().sendPacket(packet);
		}
	}
	
	public void abortDuel(Player player)
	{
		_aborted = true;
		_winner = TeamType.NONE;
		stopEvent();
	}
	
	protected IStaticPacket checkPlayer(Player player)
	{
		IStaticPacket packet = null;
		if(player.isInCombat())
		{
			packet = new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_BATTLE).addName(player);
		}
		else if(player.isDead() || player.isAlikeDead() || player.getCurrentHpPercents() < 50.0 || player.getCurrentMpPercents() < 50.0 || player.getCurrentCpPercents() < 50.0)
		{
			packet = new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1S_HP_OR_MP_IS_BELOW_50).addName(player);
		}
		else if(player.getEvent(DuelEvent.class) != null)
		{
			packet = new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_ALREADY_ENGAGED_IN_A_DUEL).addName(player);
		}
		else if(player.getEvent(ClanHallSiegeEvent.class) != null || player.getEvent(ClanHallNpcSiegeEvent.class) != null)
		{
			packet = new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_A_CLAN_HALL_WAR).addName(player);
		}
		else if(player.getEvent(SiegeEvent.class) != null)
		{
			packet = new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_A_SIEGE_WAR).addName(player);
		}
		else if(player.isOlyParticipant())
		{
			packet = new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_THE_OLYMPIAD).addName(player);
		}
		else if(player.isCursedWeaponEquipped() || player.getKarma() > 0 || player.getPvpFlag() > 0)
		{
			packet = new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_IN_A_CHAOTIC_STATE).addName(player);
		}
		else if(player.isInStoreMode())
		{
			packet = new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE).addName(player);
		}
		else if(player.isMounted() || player.isInBoat())
		{
			packet = new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_RIDING_A_BOAT_STEED_OR_STRIDER).addName(player);
		}
		else if(player.isFishing())
		{
			packet = new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_FISHING).addName(player);
		}
		else if(player.isInCombatZone() || player.isInPeaceZone() || player.isInWater() || player.isInZone(Zone.ZoneType.no_restart))
		{
			packet = new SystemMessage2(SystemMsg.C1_CANNOT_MAKE_A_CHALLENGE_TO_A_DUEL_BECAUSE_C1_IS_CURRENTLY_IN_A_DUELPROHIBITED_AREA_PEACEFUL_ZONE__SEVEN_SIGNS_ZONE__NEAR_WATER__RESTART_PROHIBITED_AREA).addName(player);
		}
		else if(player.getTransformation() != 0)
		{
			packet = new SystemMessage2(SystemMsg.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_POLYMORPHED).addName(player);
		}
		return packet;
	}
	
	protected IStaticPacket canDuel0(Player requester, Player target)
	{
		IStaticPacket packet = checkPlayer(target);
		if(packet == null && !requester.isInRangeZ(target, 1200))
		{
			packet = new SystemMessage2(SystemMsg.C1_CANNOT_RECEIVE_A_DUEL_CHALLENGE_BECAUSE_C1_IS_TOO_FAR_AWAY).addName(target);
		}
		return packet;
	}
	
	protected void updatePlayers(boolean start, boolean teleport)
	{
		for(DuelSnapshotObject dso : this)
		{
			Player player = dso.getPlayer();
			if(player == null)
				continue;
			if(teleport)
			{
				dso.teleport();
				continue;
			}
			if(start)
			{
				player.addEvent(this);
				player.setTeam(dso.getTeam());
				continue;
			}
			player.removeEvent(this);
			dso.restore(_aborted);
			player.setTeam(TeamType.NONE);
		}
	}
	
	@Override
	public SystemMsg checkForAttack(Creature target, Creature attacker, Skill skill, boolean force)
	{
		if(target.getTeam() == TeamType.NONE || attacker.getTeam() == TeamType.NONE || target.getTeam() == attacker.getTeam())
		{
			return SystemMsg.INVALID_TARGET;
		}
		DuelEvent duelEvent = target.getEvent(DuelEvent.class);
		if(duelEvent == null || duelEvent != this)
		{
			return SystemMsg.INVALID_TARGET;
		}
		return null;
	}
	
	@Override
	public boolean canAttack(Creature target, Creature attacker, Skill skill, boolean force)
	{
		if(target.getTeam() == TeamType.NONE || attacker.getTeam() == TeamType.NONE || target.getTeam() == attacker.getTeam())
		{
			return false;
		}
		DuelEvent duelEvent = target.getEvent(DuelEvent.class);
		return duelEvent != null && duelEvent == this;
	}
	
	@Override
	public void onAddEvent(GameObject o)
	{
		if(o.isPlayer())
		{
			o.getPlayer().addListener(_playerExitListener);
		}
	}
	
	@Override
	public void onRemoveEvent(GameObject o)
	{
		if(o.isPlayer())
		{
			o.getPlayer().removeListener(_playerExitListener);
		}
	}
	
	@Override
	public Iterator<DuelSnapshotObject> iterator()
	{
		List<DuelSnapshotObject> blue = getObjects(BLUE_TEAM);
		List<DuelSnapshotObject> red = getObjects(RED_TEAM);
		return new JoinedIterator<>(blue.iterator(), red.iterator());
	}
	
	@Override
	public void reCalcNextTime(boolean onInit)
	{
		registerActions();
	}
	
	@Override
	public void announce(int i)
	{
		sendPacket(new SystemMessage2(SystemMsg.THE_DUEL_WILL_BEGIN_IN_S1_SECONDS).addInteger(i));
	}
	
	protected enum DuelState
	{
		EPrepare,
		EInProgress,
		EEnd;
	}
	
	private class OnPlayerExitListenerImpl implements OnPlayerExitListener
	{
		@Override
		public void onPlayerExit(Player player)
		{
			playerExit(player);
		}
	}
}