package l2.gameserver.skills.effects;

import l2.gameserver.model.Effect;
import l2.gameserver.stats.Env;

public class EffectCPDamPercent extends Effect
{
	public EffectCPDamPercent(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		if(_effected.isDead())
		{
			return;
		}
		double newCp = (100.0 - calc()) * (double) _effected.getMaxCp() / 100.0;
		newCp = Math.min(_effected.getCurrentCp(), Math.max(0.0, newCp));
		_effected.setCurrentCp(newCp);
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}