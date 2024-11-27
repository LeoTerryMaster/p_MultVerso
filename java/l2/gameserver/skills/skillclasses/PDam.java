package l2.gameserver.skills.skillclasses;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.network.l2.s2c.FinishRotating;
import l2.gameserver.network.l2.s2c.StartRotating;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.stats.Formulas;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class PDam extends Skill
{
	private final boolean _onCrit;
	private final boolean _directHp;
	private final boolean _turner;
	private final boolean _blow;
	
	public PDam(StatsSet set)
	{
		super(set);
		_onCrit = set.getBool("onCrit", false);
		_directHp = set.getBool("directHp", false);
		_turner = set.getBool("turner", false);
		_blow = set.getBool("blow", false);
	}
	
	@Override
	public boolean isBlowSkill()
	{
		return _blow;
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		boolean ss = activeChar.getChargedSoulShot() && isSSPossible();
		for(Creature target : targets)
		{
			if(target == null || target.isDead())
				continue;
			if(_turner && !target.isInvul())
			{
				target.broadcastPacket(new StartRotating(target, target.getHeading(), 1, 65535));
				target.broadcastPacket(new FinishRotating(target, activeChar.getHeading(), 65535));
				target.setHeading(activeChar.getHeading());
				target.sendPacket(new SystemMessage(110).addSkillName(_displayId, _displayLevel));
			}
			boolean reflected;
			Creature realTarget = (reflected = target.checkReflectSkill(activeChar, this)) ? activeChar : target;
			Formulas.AttackInfo info = Formulas.calcPhysDam(activeChar, realTarget, this, false, _blow, ss, _onCrit);
			if(info.lethal_dmg > 0.0)
			{
				realTarget.reduceCurrentHp(info.lethal_dmg, activeChar, this, true, true, false, false, false, false, false);
			}
			if(!info.miss || info.damage >= 1.0)
			{
				realTarget.reduceCurrentHp(info.damage, activeChar, this, true, true, !info.lethal && _directHp, true, false, false, getPower() != 0.0);
			}
			if(!reflected)
			{
				realTarget.doCounterAttack(this, activeChar, _blow);
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
	}
}