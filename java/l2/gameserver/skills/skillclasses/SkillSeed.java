package l2.gameserver.skills.skillclasses;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Effect;
import l2.gameserver.model.Skill;
import l2.gameserver.skills.effects.EffectSkillSeed;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class SkillSeed extends Skill
{
	public SkillSeed(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		if(activeChar.isAlikeDead())
		{
			return;
		}
		for(Creature target : targets)
		{
			if(target.isAlikeDead() && getTargetType() != Skill.SkillTargetType.TARGET_CORPSE)
				continue;
			List<Effect> effects = target.getEffectList().getEffectsBySkill(this);
			boolean haveEffect = false;
			if(effects != null && !effects.isEmpty())
			{
				for(Effect effect : effects)
				{
					if(!(effect instanceof EffectSkillSeed))
						continue;
					EffectSkillSeed effectSeed = (EffectSkillSeed) effect;
					effectSeed.incSeeds();
					haveEffect = true;
				}
			}
			if(haveEffect)
				continue;
			getEffects(activeChar, target, false, false);
		}
	}
}