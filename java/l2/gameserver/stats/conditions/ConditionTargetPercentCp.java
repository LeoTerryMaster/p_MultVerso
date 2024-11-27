package l2.gameserver.stats.conditions;

import l2.gameserver.stats.Env;

public class ConditionTargetPercentCp extends Condition
{
	private final double _cp;
	
	public ConditionTargetPercentCp(int cp)
	{
		_cp = (double) cp / 100.0;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		return env.target != null && env.target.getCurrentCpRatio() <= _cp;
	}
}