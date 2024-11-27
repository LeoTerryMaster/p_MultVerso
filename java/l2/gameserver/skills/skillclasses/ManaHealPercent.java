package l2.gameserver.skills.skillclasses;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.stats.Stats;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class ManaHealPercent extends Skill
{
	private final boolean _ignoreMpEff;
	
	public ManaHealPercent(StatsSet set)
	{
		super(set);
		_ignoreMpEff = set.getBool("ignoreMpEff", true);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
		{
			if(target == null || target.isDead() || target.isHealBlocked())
				continue;
			getEffects(activeChar, target, getActivateRate() > 0, false);
			double mp = _power * (double) target.getMaxMp() / 100.0;
			double newMp = mp * (!_ignoreMpEff ? target.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100.0, activeChar, this) : 100.0) / 100.0;
			double addToMp = Math.max(0.0, Math.min(newMp, target.calcStat(Stats.MP_LIMIT, null, null) * (double) target.getMaxMp() / 100.0 - target.getCurrentMp()));
			if(addToMp > 0.0)
			{
				target.setCurrentMp(target.getCurrentMp() + addToMp);
			}
			if(!target.isPlayer())
				continue;
			if(activeChar != target)
			{
				target.sendPacket(new SystemMessage(1069).addString(activeChar.getName()).addNumber(Math.round(addToMp)));
				continue;
			}
			activeChar.sendPacket(new SystemMessage(1068).addNumber(Math.round(addToMp)));
		}
		if(isSSPossible())
		{
			activeChar.unChargeShots(isMagic());
		}
	}
}