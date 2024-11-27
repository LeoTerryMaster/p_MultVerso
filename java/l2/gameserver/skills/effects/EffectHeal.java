package l2.gameserver.skills.effects;

import l2.gameserver.model.Effect;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.stats.Env;
import l2.gameserver.stats.Stats;

public class EffectHeal extends Effect
{
	private final boolean _ignoreHpEff;
	
	public EffectHeal(Env env, EffectTemplate template)
	{
		super(env, template);
		_ignoreHpEff = template.getParam().getBool("ignoreHpEff", false);
	}
	
	@Override
	public boolean checkCondition()
	{
		if(_effected.isHealBlocked())
		{
			return false;
		}
		return super.checkCondition();
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		if(_effected.isHealBlocked())
		{
			return;
		}
		double hp = calc();
		double newHp = hp * (!_ignoreHpEff ? _effected.calcStat(Stats.HEAL_EFFECTIVNESS, 100.0, _effector, getSkill()) : 100.0) / 100.0;
		double addToHp = Math.max(0.0, Math.min(newHp, _effected.calcStat(Stats.HP_LIMIT, null, null) * (double) _effected.getMaxHp() / 100.0 - _effected.getCurrentHp()));
		if(addToHp > 0.0)
		{
			_effected.sendPacket(new SystemMessage(1066).addNumber(Math.round(addToHp)));
			_effected.setCurrentHp(addToHp + _effected.getCurrentHp(), false);
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}