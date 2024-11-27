package l2.gameserver.stats.conditions;

import l2.gameserver.stats.Env;

public class ConditionFirstEffectSuccess extends Condition
{
	boolean _param;
	
	public ConditionFirstEffectSuccess(boolean param)
	{
		_param = param;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		return _param == (env.value == 2.147483647E9);
	}
}