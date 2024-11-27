package l2.gameserver.skills.effects;

import l2.gameserver.model.Effect;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.stats.Env;
import l2.gameserver.stats.Stats;

public class EffectManaHealPercent extends Effect
{
	private final boolean _ignoreMpEff;
	
	public EffectManaHealPercent(Env env, EffectTemplate template)
	{
		super(env, template);
		_ignoreMpEff = template.getParam().getBool("ignoreMpEff", true);
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
		double mp = calc() * (double) _effected.getMaxMp() / 100.0;
		double newMp = mp * (!_ignoreMpEff ? _effected.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100.0, _effector, getSkill()) : 100.0) / 100.0;
		double addToMp = Math.max(0.0, Math.min(newMp, _effected.calcStat(Stats.MP_LIMIT, null, null) * (double) _effected.getMaxMp() / 100.0 - _effected.getCurrentMp()));
		_effected.sendPacket(new SystemMessage(1068).addNumber(Math.round(addToMp)));
		if(addToMp > 0.0)
		{
			_effected.setCurrentMp(addToMp + _effected.getCurrentMp());
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}