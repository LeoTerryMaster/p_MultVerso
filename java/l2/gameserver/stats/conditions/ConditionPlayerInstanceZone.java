package l2.gameserver.stats.conditions;

import l2.gameserver.model.entity.Reflection;
import l2.gameserver.stats.Env;

public class ConditionPlayerInstanceZone extends Condition
{
	private final int _id;
	
	public ConditionPlayerInstanceZone(int id)
	{
		_id = id;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		Reflection ref = env.character.getReflection();
		return ref.getInstancedZoneId() == _id;
	}
}