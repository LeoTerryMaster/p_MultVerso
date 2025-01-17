package l2.gameserver.skills.effects;

import l2.gameserver.model.Effect;
import l2.gameserver.stats.Env;

public final class EffectSleep extends Effect
{
	public EffectSleep(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		_effected.startSleeping();
		_effected.abortAttack(true, true);
		_effected.abortCast(true, true);
		_effected.stopMove();
	}
	
	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopSleeping();
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}