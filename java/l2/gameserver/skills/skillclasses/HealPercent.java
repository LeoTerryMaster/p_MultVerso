package l2.gameserver.skills.skillclasses;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.stats.Stats;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class HealPercent extends Skill
{
	public HealPercent(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
		{
			if(target == null || target.isHealBlocked())
				continue;
			getEffects(activeChar, target, getActivateRate() > 0, false);
			double hp = _power * (double) target.getMaxHp() / 100.0;
			double addToHp = Math.max(0.0, Math.min(hp, target.calcStat(Stats.HP_LIMIT, null, null) * (double) target.getMaxHp() / 100.0 - target.getCurrentHp()));
			if(addToHp > 0.0)
			{
				target.setCurrentHp(addToHp + target.getCurrentHp(), false);
			}
			if(!target.isPlayer())
				continue;
			if(activeChar != target)
			{
				target.sendPacket(new SystemMessage(1067).addString(activeChar.getName()).addNumber(Math.round(addToHp)));
				continue;
			}
			activeChar.sendPacket(new SystemMessage(1066).addNumber(Math.round(addToHp)));
		}
		if(isSSPossible())
		{
			activeChar.unChargeShots(isMagic());
		}
	}
}