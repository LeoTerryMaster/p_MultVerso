package l2.gameserver.stats.conditions;

import l2.gameserver.stats.Env;

public class ConditionPlayerPercentHp extends Condition
{
	private final double _hp;
	
	public ConditionPlayerPercentHp(int hp)
	{
		_hp = (double) hp / 100.0;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		return env.character.getCurrentHpRatio() <= _hp;
	}
}