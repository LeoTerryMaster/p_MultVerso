package l2.gameserver.skills.skillclasses;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class Effect extends Skill
{
	public Effect(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
		{
			if(target == null)
				continue;
			getEffects(activeChar, target, false, false);
		}
	}
}