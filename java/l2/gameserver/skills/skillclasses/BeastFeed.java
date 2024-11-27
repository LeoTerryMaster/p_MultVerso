package l2.gameserver.skills.skillclasses;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.FeedableBeastInstance;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class BeastFeed extends Skill
{
	public BeastFeed(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
		{
			if(!(target instanceof FeedableBeastInstance))
				continue;
			((FeedableBeastInstance) target).onSkillUse((Player) activeChar, _id);
		}
	}
}