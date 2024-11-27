package l2.gameserver.skills.skillclasses;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.stats.Formulas;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class LethalShot extends Skill
{
	public LethalShot(StatsSet set)
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
			boolean reflected = target.checkReflectSkill(activeChar, this);
			Creature realTarget;
			Creature creature = realTarget = reflected ? activeChar : target;
			if(getPower() > 0.0)
			{
				Formulas.AttackInfo info = Formulas.calcPhysDam(activeChar, realTarget, this, false, false, ss, false);
				if(info.lethal_dmg > 0.0)
				{
					realTarget.reduceCurrentHp(info.lethal_dmg, activeChar, this, true, true, false, false, false, false, false);
				}
				realTarget.reduceCurrentHp(info.damage, activeChar, this, true, true, false, true, false, false, true);
				if(!reflected)
				{
					realTarget.doCounterAttack(this, activeChar, false);
				}
			}
			getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
		}
	}
}