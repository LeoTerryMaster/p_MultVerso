package l2.gameserver.stats.conditions;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Effect;
import l2.gameserver.skills.EffectType;
import l2.gameserver.stats.Env;

public class ConditionPlayerHasBuff extends Condition
{
	private final EffectType _effectType;
	private final int _level;
	
	public ConditionPlayerHasBuff(EffectType effectType, int level)
	{
		_effectType = effectType;
		_level = level;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		Creature character = env.character;
		if(character == null)
		{
			return false;
		}
		Effect effect = character.getEffectList().getEffectByType(_effectType);
		if(effect == null)
		{
			return false;
		}
		return _level == -1 || effect.getSkill().getLevel() >= _level;
	}
}