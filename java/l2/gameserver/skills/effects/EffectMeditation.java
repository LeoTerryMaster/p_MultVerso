package l2.gameserver.skills.effects;

import l2.gameserver.model.Effect;
import l2.gameserver.stats.Env;

public final class EffectMeditation extends Effect
{
	public EffectMeditation(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		_effected.startParalyzed();
		_effected.setMeditated(true);
	}
	
	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopParalyzed();
		_effected.setMeditated(false);
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}