package l2.gameserver.stats.conditions;

import l2.gameserver.GameTimeController;
import l2.gameserver.stats.Env;

public class ConditionGameTime extends Condition
{
	private final CheckGameTime _check;
	private final boolean _required;
	
	public ConditionGameTime(CheckGameTime check, boolean required)
	{
		_check = check;
		_required = required;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		switch(_check)
		{
			case NIGHT:
			{
				return GameTimeController.getInstance().isNowNight() == _required;
			}
		}
		return !_required;
	}
	
	public enum CheckGameTime
	{
		NIGHT;
	}
}