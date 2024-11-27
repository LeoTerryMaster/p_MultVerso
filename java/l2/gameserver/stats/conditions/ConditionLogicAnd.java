package l2.gameserver.stats.conditions;

import l2.gameserver.stats.Env;

public class ConditionLogicAnd extends Condition
{
	private static final Condition[] emptyConditions = new Condition[0];
	public Condition[] _conditions = emptyConditions;
	
	public void add(Condition condition)
	{
		if(condition == null)
		{
			return;
		}
		int len = _conditions.length;
		Condition[] tmp = new Condition[len + 1];
		System.arraycopy(_conditions, 0, tmp, 0, len);
		tmp[len] = condition;
		_conditions = tmp;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		for(Condition c : _conditions)
		{
			if(c.test(env))
				continue;
			return false;
		}
		return true;
	}
}