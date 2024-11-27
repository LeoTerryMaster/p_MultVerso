package l2.gameserver.stats.conditions;

import l2.gameserver.stats.Env;

public class ConditionPlayerPercentCp extends Condition
{
	private final double _cp;
	
	public ConditionPlayerPercentCp(int cp)
	{
		_cp = (double) cp / 100.0;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		return env.character.getCurrentCpRatio() <= _cp;
	}
}