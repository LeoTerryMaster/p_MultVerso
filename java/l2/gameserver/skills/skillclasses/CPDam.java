package l2.gameserver.skills.skillclasses;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class CPDam extends Skill
{
	public CPDam(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		boolean ss;
		boolean bl = ss = activeChar.getChargedSoulShot() && isSSPossible();
		if(ss)
		{
			activeChar.unChargeShots(false);
		}
		for(Creature target : targets)
		{
			if(target == null || target.isDead())
				continue;
			target.doCounterAttack(this, activeChar, false);
			boolean reflected = target.checkReflectSkill(activeChar, this);
			Creature realTarget = reflected ? activeChar : target;
			if(realTarget.isCurrentCpZero())
				continue;
			double damage = _power * realTarget.getCurrentCp();
			if(damage < 1.0)
			{
				damage = 1.0;
			}
			realTarget.reduceCurrentHp(damage, activeChar, this, true, true, false, true, false, false, true);
			getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
		}
	}
}