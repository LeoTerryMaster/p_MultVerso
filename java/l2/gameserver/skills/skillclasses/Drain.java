package l2.gameserver.skills.skillclasses;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.stats.Formulas;
import l2.gameserver.stats.Stats;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class Drain extends Skill
{
	private final double _absorbAbs;
	
	public Drain(StatsSet set)
	{
		super(set);
		_absorbAbs = set.getDouble("absorbAbs", 0.0);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		int sps = isSSPossible() ? activeChar.getChargedSpiritShot() : 0;
		boolean ss = isSSPossible() && activeChar.getChargedSoulShot();
		boolean corpseSkill = _targetType == Skill.SkillTargetType.TARGET_CORPSE;
		for(Creature target : targets)
		{
			if(target == null)
				continue;
			boolean reflected = !corpseSkill && target.checkReflectSkill(activeChar, this);
			Creature realTarget;
			Creature creature = realTarget = reflected ? activeChar : target;
			if(getPower() > 0.0 || _absorbAbs > 0.0)
			{
				if(realTarget.isDead() && !corpseSkill)
					continue;
				double hp = 0.0;
				double targetHp = realTarget.getCurrentHp();
				if(!corpseSkill)
				{
					double damage;
					if(isMagic())
					{
						damage = Formulas.calcMagicDam(activeChar, realTarget, this, sps);
					}
					else
					{
						Formulas.AttackInfo info = Formulas.calcPhysDam(activeChar, realTarget, this, false, false, ss, false);
						damage = info.damage;
						if(info.lethal_dmg > 0.0)
						{
							realTarget.reduceCurrentHp(info.lethal_dmg, activeChar, this, true, true, false, false, false, false, false);
						}
					}
					double targetCP = realTarget.getCurrentCp();
					if(damage > targetCP || !realTarget.isPlayer())
					{
						hp = (damage - targetCP) * _absorbPart;
					}
					realTarget.reduceCurrentHp(damage, activeChar, this, true, true, false, true, false, false, true);
					if(!reflected)
					{
						realTarget.doCounterAttack(this, activeChar, false);
					}
				}
				if(_absorbAbs == 0.0 && _absorbPart == 0.0)
					continue;
				if((hp += _absorbAbs) > targetHp && !corpseSkill)
				{
					hp = targetHp;
				}
				double addToHp;
				if((addToHp = Math.max(0.0, Math.min(hp, activeChar.calcStat(Stats.HP_LIMIT, null, null) * (double) activeChar.getMaxHp() / 100.0 - activeChar.getCurrentHp()))) > 0.0 && !activeChar.isHealBlocked())
				{
					activeChar.setCurrentHp(activeChar.getCurrentHp() + addToHp, false);
				}
				if(realTarget.isDead() && corpseSkill && realTarget.isNpc())
				{
					activeChar.getAI().setAttackTarget(null);
					((NpcInstance) realTarget).endDecayTask();
				}
			}
			getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
		}
		if(isMagic() ? sps != 0 : ss)
		{
			activeChar.unChargeShots(isMagic());
		}
	}
}