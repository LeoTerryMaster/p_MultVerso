package l2.gameserver.skills.effects;

import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.cache.Msg;
import l2.gameserver.geodata.GeoEngine;
import l2.gameserver.model.Effect;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.events.impl.SiegeEvent;
import l2.gameserver.model.instances.SummonInstance;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.stats.Env;
import l2.gameserver.utils.PositionUtils;

public final class EffectFear extends Effect
{
	public static final double FEAR_RANGE = 900.0;
	
	public EffectFear(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean checkCondition()
	{
		if(_effected.isFearImmune())
		{
			getEffector().sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}
		Player player = _effected.getPlayer();
		if(player != null)
		{
			SiegeEvent siegeEvent = player.getEvent(SiegeEvent.class);
			if(_effected.isSummon() && siegeEvent != null && siegeEvent.containsSiegeSummon((SummonInstance) _effected))
			{
				getEffector().sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
				return false;
			}
		}
		if(_effected.isInZonePeace())
		{
			getEffector().sendPacket(Msg.YOU_MAY_NOT_ATTACK_IN_A_PEACEFUL_ZONE);
			return false;
		}
		return super.checkCondition();
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		if(!_effected.startFear())
		{
			_effected.abortAttack(true, true);
			_effected.abortCast(true, true);
			_effected.stopMove();
		}
		double angle = Math.toRadians(PositionUtils.calculateAngleFrom(_effector, _effected));
		int oldX = _effected.getX();
		int oldY = _effected.getY();
		int x = oldX + (int) (900.0 * Math.cos(angle));
		int y = oldY + (int) (900.0 * Math.sin(angle));
		_effected.setRunning();
		_effected.moveToLocation(GeoEngine.moveCheck(oldX, oldY, _effected.getZ(), x, y, _effected.getGeoIndex()), 0, false);
	}
	
	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopFear();
		_effected.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}