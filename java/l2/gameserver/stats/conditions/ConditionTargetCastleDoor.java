package l2.gameserver.stats.conditions;

import l2.gameserver.model.instances.DoorInstance;
import l2.gameserver.stats.Env;

public class ConditionTargetCastleDoor extends Condition
{
	private final boolean _isCastleDoor;
	
	public ConditionTargetCastleDoor(boolean isCastleDoor)
	{
		_isCastleDoor = isCastleDoor;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		return env.target instanceof DoorInstance == _isCastleDoor;
	}
}