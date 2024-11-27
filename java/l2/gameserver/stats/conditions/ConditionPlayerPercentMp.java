package l2.gameserver.stats.conditions;

import l2.gameserver.stats.Env;

public class ConditionPlayerPercentMp extends Condition
{
	private final double _mp;
	
	public ConditionPlayerPercentMp(int mp)
	{
		_mp = (double) mp / 100.0;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		return env.character.getCurrentMpRatio() <= _mp;
	}
}