package l2.gameserver.skills.effects;

import l2.gameserver.model.Effect;
import l2.gameserver.stats.Env;
import l2.gameserver.stats.Stats;

public class EffectManaHealOverTime extends Effect
{
	private final boolean _ignoreMpEff;
	
	public EffectManaHealOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
		_ignoreMpEff = template.getParam().getBool("ignoreMpEff", false);
	}
	
	@Override
	public boolean onActionTime()
	{
		if(_effected.isHealBlocked())
		{
			return true;
		}
		double mp = calc();
		double newMp = mp * (!_ignoreMpEff ? _effected.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100.0, _effector, getSkill()) : 100.0) / 100.0;
		double addToMp = Math.max(0.0, Math.min(newMp, _effected.calcStat(Stats.MP_LIMIT, null, null) * (double) _effected.getMaxMp() / 100.0 - _effected.getCurrentMp()));
		if(addToMp > 0.0)
		{
			_effected.setCurrentMp(_effected.getCurrentMp() + addToMp);
		}
		return true;
	}
}