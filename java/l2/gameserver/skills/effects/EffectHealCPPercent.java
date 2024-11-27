package l2.gameserver.skills.effects;

import l2.gameserver.model.Effect;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.stats.Env;
import l2.gameserver.stats.Stats;

public class EffectHealCPPercent extends Effect
{
	private final boolean _ignoreCpEff;
	
	public EffectHealCPPercent(Env env, EffectTemplate template)
	{
		super(env, template);
		_ignoreCpEff = template.getParam().getBool("ignoreCpEff", true);
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
		double cp = calc() * (double) _effected.getMaxCp() / 100.0;
		double newCp = cp * (!_ignoreCpEff ? _effected.calcStat(Stats.CPHEAL_EFFECTIVNESS, 100.0, _effector, getSkill()) : 100.0) / 100.0;
		double addToCp = Math.max(0.0, Math.min(newCp, _effected.calcStat(Stats.CP_LIMIT, null, null) * (double) _effected.getMaxCp() / 100.0 - _effected.getCurrentCp()));
		if(_effected == _effector)
		{
			_effected.sendPacket(new SystemMessage(1405).addNumber((long) addToCp));
		}
		else
		{
			_effected.sendPacket(new SystemMessage(1406).addName(_effector).addNumber((long) addToCp));
		}
		if(addToCp > 0.0)
		{
			_effected.setCurrentCp(addToCp + _effected.getCurrentCp());
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}