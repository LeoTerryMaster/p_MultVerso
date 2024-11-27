package l2.gameserver.stats.conditions;

import l2.gameserver.stats.Env;

public class ConditionZoneName extends Condition
{
	private final String _zoneName;
	
	public ConditionZoneName(String zoneName)
	{
		_zoneName = zoneName;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		if(!env.character.isPlayer())
		{
			return false;
		}
		return env.character.isInZone(_zoneName);
	}
}