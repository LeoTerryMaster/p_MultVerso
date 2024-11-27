package l2.gameserver.skills.skillclasses;

import l2.gameserver.Config;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.stats.Stats;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class ManaHeal extends Skill
{
	private final boolean _ignoreMpEff;
	
	public ManaHeal(StatsSet set)
	{
		super(set);
		_ignoreMpEff = set.getBool("ignoreMpEff", false);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		int sps;
		double mp = _power;
		int n = sps = isSSPossible() ? activeChar.getChargedSpiritShot() : 0;
		if(sps > 0 && Config.MANAHEAL_SPS_BONUS)
		{
			mp *= sps == 2 ? 1.5 : 1.3;
		}
		for(Creature target : targets)
		{
			if(target.isHealBlocked())
				continue;
			double newMp = activeChar == target ? mp : Math.min(mp * 1.7D, mp * (!_ignoreMpEff ? target.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100.0D, activeChar, this) : 100.0D) / 100.0D);
			if(getMagicLevel() > 0 && activeChar != target)
			{
				int diff = target.getLevel() - getMagicLevel();
				if(diff > 5)
				{
					if(diff < 20)
					{
						newMp = newMp / 100.0D * (double) (100 - diff * 5);
					}
					else
					{
						newMp = 0.0D;
					}
				}
			}
			
			if(newMp == 0.0)
			{
				activeChar.sendPacket(new SystemMessage(1597).addSkillName(_id, getDisplayLevel()));
				getEffects(activeChar, target, getActivateRate() > 0, false);
				continue;
			}
			double addToMp = Math.max(0.0, Math.min(newMp, target.calcStat(Stats.MP_LIMIT, null, null) * (double) target.getMaxMp() / 100.0 - target.getCurrentMp()));
			if(addToMp > 0.0)
			{
				target.setCurrentMp(addToMp + target.getCurrentMp());
			}
			if(target.isPlayer())
			{
				if(activeChar != target)
				{
					target.sendPacket(new SystemMessage(1069).addString(activeChar.getName()).addNumber(Math.round(addToMp)));
				}
				else
				{
					activeChar.sendPacket(new SystemMessage(1068).addNumber(Math.round(addToMp)));
				}
			}
			getEffects(activeChar, target, getActivateRate() > 0, false);
		}
		if(isSSPossible())
		{
			activeChar.unChargeShots(isMagic());
		}
	}
}