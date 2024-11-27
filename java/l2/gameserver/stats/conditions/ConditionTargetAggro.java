package l2.gameserver.stats.conditions;

import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.stats.Env;

public class ConditionTargetAggro extends Condition
{
	private final boolean _isAggro;
	
	public ConditionTargetAggro(boolean isAggro)
	{
		_isAggro = isAggro;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		Creature target = env.target;
		if(target == null)
		{
			return false;
		}
		if(target.isMonster())
		{
			return ((MonsterInstance) target).isAggressive() == _isAggro;
		}
		if(target.isPlayer())
		{
			return target.getKarma() > 0;
		}
		return false;
	}
}