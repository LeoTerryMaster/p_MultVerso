package l2.gameserver.stats.conditions;

import l2.gameserver.stats.Env;

public class ConditionPlayerChargesMin extends Condition
{
	private final int _minCharges;
	
	public ConditionPlayerChargesMin(int minCharges)
	{
		_minCharges = minCharges;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		if(env.character == null || !env.character.isPlayer())
		{
			return false;
		}
		return env.character.getIncreasedForce() >= _minCharges;
	}
}