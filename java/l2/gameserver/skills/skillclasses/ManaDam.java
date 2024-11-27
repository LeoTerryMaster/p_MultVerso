package l2.gameserver.skills.skillclasses;

import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.stats.Formulas;
import l2.gameserver.stats.Stats;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class ManaDam extends Skill
{
	public ManaDam(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		int sps = 0;
		if(isSSPossible())
		{
			sps = activeChar.getChargedSpiritShot();
		}
		for(Creature target : targets)
		{
			if(target == null || target.isDead())
				continue;
			int magicLevel = getMagicLevel() == 0 ? activeChar.getLevel() : getMagicLevel();
			int landRate = Rnd.get(30, 100);
			landRate *= target.getLevel();
			landRate /= magicLevel;
			if(Rnd.chance(landRate))
			{
				double mAtk = activeChar.getMAtk(target, this);
				if(sps == 2)
				{
					mAtk *= 4.0;
				}
				else if(sps == 1)
				{
					mAtk *= 2.0;
				}
				double mDef = target.getMDef(activeChar, this);
				if(mDef < 1.0)
				{
					mDef = 1.0;
				}
				double damage = Math.sqrt(mAtk) * getPower() * (double) (target.getMaxMp() / 97) / mDef;
				if(Config.MDAM_CRIT_POSSIBLE)
				{
					boolean crit = Formulas.calcMCrit(activeChar, target, activeChar.getMagicCriticalRate(target, this));
					if(crit)
					{
						activeChar.sendPacket(Msg.MAGIC_CRITICAL_HIT);
						damage *= activeChar.calcStat(Stats.MCRITICAL_DAMAGE, 4.0D, target, this);
					}
				}
				target.reduceCurrentMp(damage, activeChar, true);
			}
			else
			{
				SystemMessage msg = new SystemMessage(159).addName(target);
				activeChar.sendPacket(msg);
				target.sendPacket(msg);
				target.reduceCurrentHp(1.0, activeChar, this, true, true, false, true, false, false, true);
			}
			getEffects(activeChar, target, getActivateRate() > 0, false);
		}
		if(isSSPossible())
		{
			activeChar.unChargeShots(isMagic());
		}
	}
}