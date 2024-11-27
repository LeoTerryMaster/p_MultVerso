package l2.gameserver.stats.conditions;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Effect;
import l2.gameserver.skills.effects.EffectSkillSeed;
import l2.gameserver.stats.Env;

import java.util.List;

public class ConditionPlayerSkillMinSeed extends Condition
{
	private final int _skillId;
	private final int _skillMinSeed;
	
	public ConditionPlayerSkillMinSeed(int skillId, int skillMinSeed)
	{
		_skillId = skillId;
		_skillMinSeed = skillMinSeed;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		Creature activeChar = env.character;
		if(activeChar == null)
		{
			return false;
		}
		List<Effect> effects = activeChar.getEffectList().getEffectsBySkillId(_skillId);
		if(effects == null || effects.isEmpty())
		{
			return false;
		}
		for(Effect effect : effects)
		{
			if(effect instanceof EffectSkillSeed)
			{
				EffectSkillSeed effectSeed = (EffectSkillSeed) effect;
				if(effectSeed.getSeeds() >= _skillMinSeed)
				{
					return true;
				}
			}
		}
		return false;
	}
}