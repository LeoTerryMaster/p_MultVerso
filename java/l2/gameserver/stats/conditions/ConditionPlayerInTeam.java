package l2.gameserver.stats.conditions;

import l2.gameserver.model.Player;
import l2.gameserver.model.base.TeamType;
import l2.gameserver.stats.Env;

public class ConditionPlayerInTeam extends Condition
{
	private final boolean _value;
	
	public ConditionPlayerInTeam(boolean value)
	{
		_value = value;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		if(env.character == null)
		{
			return false;
		}
		Player player = env.character.getPlayer();
		return player != null && player.getTeam() != TeamType.NONE == _value;
	}
}