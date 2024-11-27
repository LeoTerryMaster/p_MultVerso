package l2.gameserver.skills.skillclasses;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.stats.Stats;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class CombatPointHeal extends Skill
{
	private final boolean _ignoreCpEff;
	
	public CombatPointHeal(StatsSet set)
	{
		super(set);
		_ignoreCpEff = set.getBool("ignoreCpEff", false);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
		{
			if(target == null || target.isDead() || target.isHealBlocked())
				continue;
			double maxNewCp = _power * (!_ignoreCpEff ? target.calcStat(Stats.CPHEAL_EFFECTIVNESS, 100.0, activeChar, this) : 100.0) / 100.0;
			double addToCp = Math.max(0.0, Math.min(maxNewCp, target.calcStat(Stats.CP_LIMIT, null, null) * (double) target.getMaxCp() / 100.0 - target.getCurrentCp()));
			if(addToCp > 0.0)
			{
				target.setCurrentCp(addToCp + target.getCurrentCp());
			}
			target.sendPacket(new SystemMessage(1405).addNumber((long) addToCp));
			getEffects(activeChar, target, getActivateRate() > 0, false);
		}
		if(isSSPossible())
		{
			activeChar.unChargeShots(isMagic());
		}
	}
}