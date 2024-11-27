package l2.gameserver.skills.skillclasses;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.model.Summon;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.stats.Formulas;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class DestroySummon extends Skill
{
	public DestroySummon(StatsSet set)
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
			if(getActivateRate() > 0 && !Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate()))
			{
				activeChar.sendPacket(new SystemMessage(139).addString(target.getName()).addSkillName(getId(), getLevel()));
				continue;
			}
			if(!target.isSummon())
				continue;
			((Summon) target).saveEffects();
			((Summon) target).unSummon();
			getEffects(activeChar, target, getActivateRate() > 0, false);
		}
		if(isSSPossible())
		{
			activeChar.unChargeShots(isMagic());
		}
	}
}