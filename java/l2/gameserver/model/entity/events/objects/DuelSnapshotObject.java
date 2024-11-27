package l2.gameserver.model.entity.events.objects;

import l2.commons.lang.reference.HardReference;
import l2.commons.threading.RunnableImpl;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.model.Effect;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.TeamType;
import l2.gameserver.stats.Env;
import l2.gameserver.utils.Location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DuelSnapshotObject implements Serializable
{
	private final TeamType _team;
	private final HardReference<Player> _playerRef;
	private final int _activeClass;
	private final List<Effect> _effects;
	private final Location _returnLoc;
	private final double _currentHp;
	private final double _currentMp;
	private final double _currentCp;
	private boolean _isDead;
	
	public DuelSnapshotObject(Player player, TeamType team)
	{
		_playerRef = player.getRef();
		_team = team;
		_returnLoc = player.getReflection().getReturnLoc() == null ? player.getLoc() : player.getReflection().getReturnLoc();
		_currentCp = player.getCurrentCp();
		_currentHp = player.getCurrentHp();
		_currentMp = player.getCurrentMp();
		_activeClass = player.getActiveClassId();
		List<Effect> effectList = player.getEffectList().getAllEffects();
		_effects = new ArrayList<>(effectList.size());
		for(Effect playerEffect : effectList)
		{
			Effect newEffect = playerEffect.getTemplate().getEffect(new Env(playerEffect.getEffector(), playerEffect.getEffected(), playerEffect.getSkill()));
			if(!newEffect.isSaveable())
				continue;
			newEffect.setCount(playerEffect.getCount());
			newEffect.setPeriod(playerEffect.getCount() == 1 ? playerEffect.getPeriod() - playerEffect.getTime() : playerEffect.getPeriod());
			_effects.add(newEffect);
		}
	}
	
	public void restore(boolean abnormal)
	{
		Player player = getPlayer();
		if(player == null)
		{
			return;
		}
		if(!abnormal)
		{
			player.getEffectList().stopAllEffects();
			if(_activeClass == player.getActiveClassId())
			{
				for(Effect e : _effects)
				{
					if(player.getEffectList().getEffectsBySkill(e.getSkill()) != null)
						continue;
					player.getEffectList().addEffect(e);
				}
			}
			player.setCurrentCp(_currentCp);
			player.setCurrentHpMp(_currentHp, _currentMp);
		}
	}
	
	public void teleport()
	{
		Player player = getPlayer();
		player._stablePoint = null;
		if(player.isFrozen())
		{
			player.stopFrozen();
		}
		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			
			@Override
			public void runImpl() throws Exception
			{
				Player player = getPlayer();
				if(player == null)
				{
					return;
				}
				player.teleToLocation(_returnLoc, ReflectionManager.DEFAULT);
			}
		}, 5000);
	}
	
	public Player getPlayer()
	{
		return _playerRef.get();
	}
	
	public boolean isDead()
	{
		return _isDead;
	}
	
	public void setDead()
	{
		_isDead = true;
	}
	
	public Location getLoc()
	{
		return _returnLoc;
	}
	
	public TeamType getTeam()
	{
		return _team;
	}
}