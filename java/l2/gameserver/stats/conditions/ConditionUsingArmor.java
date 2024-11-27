package l2.gameserver.stats.conditions;

import l2.gameserver.model.Player;
import l2.gameserver.stats.Env;
import l2.gameserver.templates.item.ArmorTemplate;

public class ConditionUsingArmor extends Condition
{
	private final ArmorTemplate.ArmorType _armor;
	
	public ConditionUsingArmor(ArmorTemplate.ArmorType armor)
	{
		_armor = armor;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		return env.character.isPlayer() && ((Player) env.character).isWearingArmor(_armor);
	}
}