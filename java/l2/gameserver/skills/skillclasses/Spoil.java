package l2.gameserver.skills.skillclasses;

import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.stats.Formulas;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class Spoil extends Skill
{
	public Spoil(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		if(!activeChar.isPlayer())
		{
			return;
		}
		int ss = isSSPossible() ? isMagic() ? activeChar.getChargedSpiritShot() : activeChar.getChargedSoulShot() ? 2 : 0 : 0;
		if(ss > 0 && getPower() > 0.0D)
		{
			activeChar.unChargeShots(false);
		}
		for(Creature target : targets)
		{
			if(target == null || target.isDead())
				continue;
			if(target.isMonster())
			{
				if(((MonsterInstance) target).isSpoiled())
				{
					activeChar.sendPacket(Msg.ALREADY_SPOILED);
				}
				else
				{
					MonsterInstance monster = (MonsterInstance) target;
					int monsterLevel = monster.getLevel();
					int modifier = monsterLevel - getMagicLevel();
					double rateOfSpoil = Math.max(getActivateRate(), 80);
					if(modifier > 8)
					{
						rateOfSpoil -= rateOfSpoil * (double) (modifier - 8) * 9.0 / 100.0;
					}
					rateOfSpoil *= (double) getMagicLevel() / (double) monsterLevel;
					rateOfSpoil = Math.max(Config.MINIMUM_SPOIL_RATE, Math.min(rateOfSpoil, 99.0));
					boolean success = Rnd.chance(rateOfSpoil);
					if(success && monster.setSpoiled((Player) activeChar))
					{
						activeChar.sendPacket(Msg.THE_SPOIL_CONDITION_HAS_BEEN_ACTIVATED);
					}
				}
			}
			if(getPower() > 0.0)
			{
				double damage;
				if(isMagic())
				{
					damage = Formulas.calcMagicDam(activeChar, target, this, ss);
				}
				else
				{
					Formulas.AttackInfo info = Formulas.calcPhysDam(activeChar, target, this, false, false, ss > 0, false);
					damage = info.damage;
					if(info.lethal_dmg > 0.0)
					{
						target.reduceCurrentHp(info.lethal_dmg, activeChar, this, true, true, false, false, false, false, false);
					}
				}
				target.reduceCurrentHp(damage, activeChar, this, true, true, false, true, false, false, true);
				target.doCounterAttack(this, activeChar, false);
			}
			getEffects(activeChar, target, false, false);
			target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, Math.max(_effectPoint, 1));
		}
	}
}