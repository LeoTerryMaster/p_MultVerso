package l2.gameserver.stats.conditions;

import l2.gameserver.stats.Env;

public class ConditionUsingSkill extends Condition
{
	private final int _id;
	
	public ConditionUsingSkill(int id)
	{
		_id = id;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		if(env.skill == null)
		{
			return false;
		}
		return env.skill.getId() == _id;
	}
}