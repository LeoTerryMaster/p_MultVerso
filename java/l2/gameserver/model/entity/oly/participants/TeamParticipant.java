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

import java.util.ArrayList;

public class TeamParticipant extends Participant
{
	private final HardReference<Player>[] _playerRefs;
	private final int[] _playerClassIds;
	private final double[] _damage;
	private final boolean[] _alive;
	private String _name = "";
	
	public TeamParticipant(int side, Competition comp, Player[] players)
	{
		super(side, comp);
		_damage = new double[players.length];
		_alive = new boolean[players.length];
		_playerClassIds = new int[players.length];
		_playerRefs = new HardReference[players.length];
		_name = players[0].getName();
		for(int i = 0;i < players.length;++i)
		{
			_playerRefs[i] = players[i].getRef();
			_playerClassIds[i] = players[i].getActiveClassId();
			_damage[i] = 0.0;
			_alive[i] = true;
		}
	}
	
	@Override
	public void OnStart()
	{
		for(Player player : getPlayers())
		{
			player.setOlyParticipant(this);
		}
	}
	
	@Override
	public void OnFinish()
	{
		for(Player player : getPlayers())
		{
			if(player.isDead())
			{
				player.doRevive(100.0);
			}
			player.setOlyParticipant(null);
		}
	}
	
	@Override
	public void OnDamaged(Player player, Creature attacker, double damage, double hp)
	{
		if(player.isOlyCompetitionFinished())
		{
			return;
		}
		for(int i = 0;i < _playerRefs.length;++i)
		{
			if(!_alive[i] || _playerRefs[i].get() != player)
				continue;
			if(attacker.isPlayer())
			{
				double[] arrd = _damage;
				int n = i;
				arrd[n] = arrd[n] + Math.min(damage, hp);
			}
			if(damage < hp)
				continue;
			_alive[i] = false;
			attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			attacker.abortAttack(true, true);
			if(attacker.isCastingNow())
			{
				attacker.abortCast(true, false);
			}
			attacker.sendActionFailed();
			getCompetition().ValidateWinner();
		}
	}
	
	@Override
	public void OnDisconnect(Player player)
	{
		if(player.isOlyCompetitionFinished())
		{
			return;
		}
		for(int i = 0;i < _playerRefs.length;++i)
		{
			_alive[i] = false;
		}
		getCompetition().ValidateWinner();
	}
	
	@Override
	public void sendPacket(L2GameServerPacket gsp)
	{
		for(Player player : getPlayers())
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
		for(boolean alv : _alive)
		{
			if(!alv)
				continue;
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isPlayerLoose(Player player)
	{
		for(int i = 0;i < _playerRefs.length;++i)
		{
			if(_playerRefs[i].get() != player)
				continue;
			return !_alive[i];
		}
		return false;
	}
	
	@Override
	public double getDamageOf(Player player)
	{
		for(int i = 0;i < _playerRefs.length;++i)
		{
			if(_playerRefs[i].get() != player)
				continue;
			return _damage[i];
		}
		return 0.0;
	}
	
	@Override
	public Player[] getPlayers()
	{
		ArrayList<Player> result = new ArrayList<>();
		for(HardReference<Player> playerRef : _playerRefs)
		{
			Player player = playerRef.get();
			if(player == null)
				continue;
			result.add(player);
		}
		return result.toArray(new Player[result.size()]);
	}
	
	@Override
	public double getTotalDamage()
	{
		double rdmg = 0.0;
		for(double dmg : _damage)
		{
			rdmg += dmg;
		}
		return rdmg;
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
		for(int refIdx = 0;refIdx < _playerRefs.length;++refIdx)
		{
			Player player = _playerRefs[refIdx].get();
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
			if(player.getActiveClassId() != _playerClassIds[refIdx] || !player.getActiveClass().isBase())
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
			if(msg == null)
				continue;
			sendPacket(msg);
			oponent.sendPacket(Msg.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME);
			return false;
		}
		return true;
	}
}