package l2.gameserver.stats.conditions;

import l2.gameserver.stats.Env;

public class ConditionTargetPercentMp extends Condition
{
	private final double _mp;
	
	public ConditionTargetPercentMp(int mp)
	{
		_mp = (double) mp / 100.0;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		return env.target != null && env.target.getCurrentMpRatio() <= _mp;
	}
}