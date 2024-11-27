package l2.gameserver.skills.effects;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Effect;
import l2.gameserver.model.Playable;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.stats.Env;

public final class EffectSilentMove extends Effect
{
	public EffectSilentMove(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		if(_effected.isPlayable())
		{
			((Playable) _effected).startSilentMoving();
		}
	}
	
	@Override
	public void onExit()
	{
		super.onExit();
		if(_effected.isPlayable())
		{
			((Playable) _effected).stopSilentMoving();
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead())
		{
			return false;
		}
		if(!getSkill().isToggle())
		{
			return false;
		}
		double manaDam = calc();
		if(manaDam > _effected.getCurrentMp())
		{
			_effected.sendPacket(Msg.NOT_ENOUGH_MP);
			_effected.sendPacket(new SystemMessage(749).addSkillName(getSkill().getId(), getSkill().getDisplayLevel()));
			return false;
		}
		_effected.reduceCurrentMp(manaDam, null);
		return true;
	}
}