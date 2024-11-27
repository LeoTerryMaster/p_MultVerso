package l2.gameserver.skills.skillclasses;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.instances.SummonInstance;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.stats.Formulas;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class MDam extends Skill
{
	public MDam(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!(_targetType != Skill.SkillTargetType.TARGET_AREA_AIM_CORPSE || target != null && target.isDead() && (target.isNpc() || target.isSummon())))
		{
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
			return false;
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		int sps = isSSPossible() ? isMagic() ? activeChar.getChargedSpiritShot() : activeChar.getChargedSoulShot() ? 2 : 0 : 0;
		for(Creature target : targets)
		{
			if(target == null || target.isDead())
				continue;
			boolean reflected = target.checkReflectSkill(activeChar, this);
			Creature realTarget = reflected ? activeChar : target;
			double damage = Formulas.calcMagicDam(activeChar, realTarget, this, sps);
			if(damage >= 1.0)
			{
				realTarget.reduceCurrentHp(damage, activeChar, this, true, true, false, true, false, false, true);
			}
			getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
		}
		if(isSuicideAttack())
		{
			activeChar.doDie(null);
		}
		else if(isSSPossible())
		{
			activeChar.unChargeShots(isMagic());
		}
		Creature corpse;
		if(_targetType == Skill.SkillTargetType.TARGET_AREA_AIM_CORPSE && targets.size() > 0 && (corpse = targets.get(0)) != null && corpse.isDead())
		{
			if(corpse.isNpc())
			{
				((NpcInstance) corpse).endDecayTask();
			}
			else if(corpse.isSummon())
			{
				((SummonInstance) corpse).endDecayTask();
			}
			activeChar.getAI().setAttackTarget(null);
		}
	}
}