package l2.gameserver.skills.skillclasses;

import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.residences.SiegeFlagInstance;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.stats.Formulas;
import l2.gameserver.stats.Stats;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class Heal extends Skill
{
	private final boolean _ignoreHpEff;
	private final boolean _staticPower;
	
	public Heal(StatsSet set)
	{
		super(set);
		_ignoreHpEff = set.getBool("ignoreHpEff", false);
		_staticPower = set.getBool("staticPower", isHandler());
	}
	
	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(target == null || target.isDoor() || target instanceof SiegeFlagInstance)
		{
			return false;
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		double hp = getPower();
		if(!_staticPower)
		{
			int mAtk = activeChar.getMAtk(null, this);
			int mAtkMod = 1;
			int staticBonus = 0;
			if(isSSPossible())
			{
				switch(activeChar.getChargedSpiritShot())
				{
					case 2:
					{
						mAtkMod = 4;
						staticBonus = getStaticBonus(mAtk);
						break;
					}
					case 1:
					{
						mAtkMod = 2;
						staticBonus = getStaticBonus(mAtk) / 2;
					}
				}
			}
			hp += Math.sqrt(mAtkMod * mAtk) + (double) staticBonus;
			if(Config.HEAL_CRIT_POSSIBLE && Formulas.calcMCrit(activeChar, null, 4.5))
			{
				hp *= 3.0;
			}
		}
		for(Creature target : targets)
		{
			if(target == null || target.isHealBlocked() || target != activeChar && (target.isPlayer() && target.isCursedWeaponEquipped() || activeChar.isPlayer() && activeChar.isCursedWeaponEquipped()))
				continue;
			double addToHp;
			if(_staticPower)
			{
				addToHp = _power;
			}
			else
			{
				addToHp = hp;
				if(!isHandler())
				{
					addToHp += activeChar.calcStat(Stats.HEAL_POWER, activeChar, this);
					addToHp *= (!_ignoreHpEff ? target.calcStat(Stats.HEAL_EFFECTIVNESS, 100.0, activeChar, this) : 100.0) / 100.0;
				}
			}
			addToHp = Math.max(0.0, Math.min(addToHp, target.calcStat(Stats.HP_LIMIT, null, null) * (double) target.getMaxHp() / 100.0 - target.getCurrentHp()));
			if(addToHp > 0.0)
			{
				target.setCurrentHp(addToHp + target.getCurrentHp(), false);
			}
			if(target.isPlayer())
			{
				if(getId() == 4051)
				{
					target.sendPacket(Msg.REJUVENATING_HP);
				}
				else if(activeChar == target)
				{
					activeChar.sendPacket(new SystemMessage(SystemMsg.S1_HP_HAS_BEEN_RESTORED).addNumber(Math.round(addToHp)));
				}
				else
				{
					target.sendPacket(new SystemMessage(SystemMsg.S2_HP_HAS_BEEN_RESTORED_BY_C1).addName(activeChar).addNumber(Math.round(addToHp)));
				}
			}
			getEffects(activeChar, target, getActivateRate() > 0, false);
		}
		if(isSSPossible() && isMagic())
		{
			activeChar.unChargeShots(isMagic());
		}
	}
	
	private final int getStaticBonus(int mAtk)
	{
		double power = getPower();
		double bottom = getPower() / 4.0;
		if((double) mAtk < bottom)
		{
			return 0;
		}
		double top = getPower() / 3.1;
		if((double) mAtk > getPower())
		{
			return (int) top;
		}
		mAtk = (int) ((double) mAtk - bottom);
		return (int) (top * ((double) mAtk / (power - bottom)));
	}
}