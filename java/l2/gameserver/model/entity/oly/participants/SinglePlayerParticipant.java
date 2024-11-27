package l2.gameserver.model.entity.oly.participants;

import l2.commons.lang.reference.HardReference;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.oly.Competition;
import l2.gameserver.model.entity.oly.Participant;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;
import l2.gameserver.network.l2.s2c.SystemMessage;

public class SinglePlayerParticipant extends Participant
{
	private final HardReference<Player> _playerRef;
	private final int _playerClassId;
	private final String _name;
	private double _damage;
	private boolean _alive;
	
	public SinglePlayerParticipant(int side, Competition comp, Player player)
	{
		super(side, comp);
		_playerRef = player.getRef();
		_playerClassId = player.getActiveClassId();
		_name = player.getName();
		_alive = true;
	}
	
	private Player getPlayer()
	{
		return _playerRef.get();
	}
	
	@Override
	public void OnStart()
	{
		Player player = getPlayer();
		if(player != null)
		{
			player.setOlyParticipant(this);
		}
	}
	
	@Override
	public void OnFinish()
	{
		Player player = getPlayer();
		if(player != null)
		{
			player.setOlyParticipant(null);
		}
	}
	
	@Override
	public void OnDamaged(Player player, Creature attacker, double damage, double hp)
	{
		if(!player.isOlyCompetitionStarted())
		{
			return;
		}
		if(attacker.isPlayer())
		{
			_damage += Math.min(damage, hp);
		}
		if(damage >= hp)
		{
			_alive = false;
			attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			attacker.abortAttack(true, true);
			if(attacker.isCastingNow())
			{
				attacker.abortCast(true, false);
			}
			attacker.sendActionFailed();
			getCompetition().ValidateWinner();
			player.setCurrentHp(1.0, false);
		}
	}
	
	@Override
	public void OnDisconnect(Player player)
	{
		if(player.isOlyCompetitionFinished())
		{
			return;
		}
		_alive = false;
		getCompetition().ValidateWinner();
	}
	
	@Override
	public void sendPacket(L2GameServerPacket gsp)
	{
		Player player = getPlayer();
		if(player != null)
		{
			player.sendPacket(gsp);
		}
	}
	
	@Override
	public String getName()
	{
		return _name;
	}
	
	@Override
	public boolean isAlive()
	{
		return _alive;
	}
	
	@Override
	public boolean isPlayerLoose(Player player)
	{
		if(player != null && player == _playerRef.get())
		{
			return !_alive;
		}
		return false;
	}
	
	@Override
	public double getDamageOf(Player player)
	{
		if(player != null && player == _playerRef.get())
		{
			return _damage;
		}
		return 0.0;
	}
	
	@Override
	public Player[] getPlayers()
	{
		if(getPlayer() != null)
		{
			return new Player[] {getPlayer()};
		}
		return new Player[0];
	}
	
	@Override
	public double getTotalDamage()
	{
		return _damage;
	}
	
	@Override
	public boolean validateThis()
	{
		Participant oponent = null;
		for(Participant p : getCompetition().getParticipants())
		{
			if(p == this)
				continue;
			oponent = p;
		}
		Player player = _playerRef.get();
		if(player == null || !player.isOnline() || player.isLogoutStarted())
		{
			oponent.sendPacket(Msg.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
			return false;
		}
		if(player.isDead())
		{
			sendPacket(new SystemMessage(1858).addName(player));
			oponent.sendPacket(Msg.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
			return false;
		}
		if(player.getActiveClassId() != _playerClassId || !player.getActiveClass().isBase())
		{
			player.sendPacket(new SystemMessage(1692).addName(player));
			oponent.sendPacket(Msg.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
			return false;
		}
		if(player.isCursedWeaponEquipped())
		{
			sendPacket(new SystemMessage(1857).addName(player).addItemName(player.getCursedWeaponEquippedId()));
			oponent.sendPacket(new SystemMessage(1856).addItemName(player.getCursedWeaponEquippedId()));
			return false;
		}
		if(!player.isInPeaceZone())
		{
			oponent.sendPacket(Msg.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
			return false;
		}
		SystemMessage msg = Competition.checkPlayer(player);
		if(msg != null)
		{
			sendPacket(msg);
			oponent.sendPacket(Msg.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
			return false;
		}
		return true;
	}
}