package l2.gameserver.skills.effects;

import l2.gameserver.model.Effect;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.stats.Env;
import l2.gameserver.stats.Stats;

public class EffectHealPercent extends Effect
{
	public EffectHealPercent(Env env, EffectTemplate template)
	{
		super(env, template);
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
		double hp = calc() * (double) _effected.getMaxHp() / 100.0;
		double addToHp = Math.max(0.0, Math.min(hp, _effected.calcStat(Stats.HP_LIMIT, null, null) * (double) _effected.getMaxHp() / 100.0 - _effected.getCurrentHp()));
		_effected.sendPacket(new SystemMessage(1066).addNumber(Math.round(addToHp)));
		if(addToHp > 0.0)
		{
			_effected.setCurrentHp(addToHp + _effected.getCurrentHp(), false);
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}