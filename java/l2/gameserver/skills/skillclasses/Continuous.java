package l2.gameserver.skills.skillclasses;

import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.stats.Stats;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class Continuous extends Skill
{
	private final int _lethal1;
	private final int _lethal2;
	
	public Continuous(StatsSet set)
	{
		super(set);
		_lethal1 = set.getInteger("lethal1", 0);
		_lethal2 = set.getInteger("lethal2", 0);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
		{
			if(target == null || getSkillType() == Skill.SkillType.BUFF && target != activeChar && (target.isCursedWeaponEquipped() || activeChar.isCursedWeaponEquipped()))
				continue;
			boolean reflected = target.checkReflectSkill(activeChar, this);
			Creature realTarget = reflected ? activeChar : target;
			double mult = 0.01 * realTarget.calcStat(Stats.DEATH_VULNERABILITY, activeChar, this);
			double lethal1 = (double) _lethal1 * mult;
			double lethal2 = (double) _lethal2 * mult;
			if(lethal1 > 0.0 && Rnd.chance(lethal1))
			{
				if(realTarget.isPlayer())
				{
					realTarget.reduceCurrentHp(realTarget.getCurrentCp(), activeChar, this, true, true, false, true, false, false, true);
					realTarget.sendPacket(SystemMsg.LETHAL_STRIKE);
					activeChar.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
				}
				else if(realTarget.isNpc() && !realTarget.isLethalImmune())
				{
					realTarget.reduceCurrentHp(realTarget.getCurrentHp() / 2.0, activeChar, this, true, true, false, true, false, false, true);
					activeChar.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
				}
			}
			else if(lethal2 > 0.0 && Rnd.chance(lethal2))
			{
				if(realTarget.isPlayer())
				{
					realTarget.reduceCurrentHp(realTarget.getCurrentHp() + realTarget.getCurrentCp() - 1.0, activeChar, this, true, true, false, true, false, false, true);
					realTarget.sendPacket(SystemMsg.LETHAL_STRIKE);
					activeChar.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
				}
				else if(realTarget.isNpc() && !realTarget.isLethalImmune())
				{
					realTarget.reduceCurrentHp(realTarget.getCurrentHp() - 1.0, activeChar, this, true, true, false, true, false, false, true);
					activeChar.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
				}
			}
			getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
		}
		if(isSSPossible() && (!Config.SAVING_SPS || _skillType != Skill.SkillType.BUFF))
		{
			activeChar.unChargeShots(isMagic());
		}
	}
}