package l2.gameserver.skills.skillclasses;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.stats.Stats;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class Balance extends Skill
{
	public Balance(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		double summaryCurrentHp = 0.0;
		int summaryMaximumHp = 0;
		for(Creature target : targets)
		{
			if(target == null || target.isAlikeDead())
				continue;
			summaryCurrentHp += target.getCurrentHp();
			summaryMaximumHp += target.getMaxHp();
		}
		double percent = summaryCurrentHp / (double) summaryMaximumHp;
		for(Creature target : targets)
		{
			if(target == null || target.isAlikeDead())
				continue;
			double hp = (double) target.getMaxHp() * percent;
			if(hp > target.getCurrentHp())
			{
				double limit = target.calcStat(Stats.HP_LIMIT, null, null) * (double) target.getMaxHp() / 100.0;
				if(target.getCurrentHp() < limit)
				{
					target.setCurrentHp(Math.min(hp, limit), false);
				}
			}
			else
			{
				target.setCurrentHp(Math.max(1.01, hp), false);
			}
			getEffects(activeChar, target, getActivateRate() > 0, false);
		}
		if(isSSPossible())
		{
			activeChar.unChargeShots(isMagic());
		}
	}
}