package l2.gameserver.stats.conditions;

import l2.gameserver.stats.Env;

public class ConditionTargetPercentHp extends Condition
{
	private final double _hp;
	
	public ConditionTargetPercentHp(int hp)
	{
		_hp = (double) hp / 100.0;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		return env.target != null && env.target.getCurrentHpRatio() <= _hp;
	}
}