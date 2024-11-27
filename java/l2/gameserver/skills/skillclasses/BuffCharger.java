package l2.gameserver.skills.skillclasses;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Effect;
import l2.gameserver.model.Skill;
import l2.gameserver.tables.SkillTable;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class BuffCharger extends Skill
{
	private final int _target;
	
	public BuffCharger(StatsSet set)
	{
		super(set);
		_target = set.getInteger("targetBuff", 0);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
		{
			int level = 0;
			List<Effect> el = target.getEffectList().getEffectsBySkillId(_target);
			if(el != null)
			{
				level = el.get(0).getSkill().getLevel();
			}
			Skill next;
			if((next = SkillTable.getInstance().getInfo(_target, level + 1)) == null)
				continue;
			next.getEffects(activeChar, target, false, false);
		}
	}
}