package l2.gameserver.skills.effects;

import l2.gameserver.model.Effect;
import l2.gameserver.stats.Env;

public class EffectHPDamPercent extends Effect
{
	public EffectHPDamPercent(Env env, EffectTemplate template)
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
		double newHp = (100.0 - calc()) * (double) _effected.getMaxHp() / 100.0;
		newHp = Math.min(_effected.getCurrentHp(), Math.max(0.0, newHp));
		_effected.setCurrentHp(newHp, false);
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}