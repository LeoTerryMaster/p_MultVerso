package l2.gameserver.stats.conditions;

import l2.gameserver.model.Player;
import l2.gameserver.stats.Env;

public class ConditionPlayerRiding extends Condition
{
	private final CheckPlayerRiding _riding;
	
	public ConditionPlayerRiding(CheckPlayerRiding riding)
	{
		_riding = riding;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		if(!env.character.isPlayer())
		{
			return false;
		}
		if(_riding == CheckPlayerRiding.STRIDER && ((Player) env.character).isRiding())
		{
			return true;
		}
		if(_riding == CheckPlayerRiding.WYVERN && env.character.isFlying())
		{
			return true;
		}
		return _riding == CheckPlayerRiding.NONE && !((Player) env.character).isRiding() && !env.character.isFlying();
	}
	
	public enum CheckPlayerRiding
	{
		NONE,
		STRIDER,
		WYVERN;
	}
}