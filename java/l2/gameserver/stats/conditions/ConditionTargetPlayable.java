package l2.gameserver.stats.conditions;

import l2.gameserver.model.Creature;
import l2.gameserver.stats.Env;

public class ConditionTargetPlayable extends Condition
{
	private final boolean _flag;
	
	public ConditionTargetPlayable(boolean flag)
	{
		_flag = flag;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		Creature target = env.target;
		return target != null && target.isPlayable() == _flag;
	}
}