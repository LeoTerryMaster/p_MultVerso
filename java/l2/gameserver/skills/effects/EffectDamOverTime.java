package l2.gameserver.skills.effects;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Effect;
import l2.gameserver.stats.Env;
import l2.gameserver.stats.Stats;

public class EffectDamOverTime extends Effect
{
	private static final int[] bleed = {12, 17, 25, 34, 44, 54, 62, 67, 72, 77, 82, 87};
	private static final int[] poison = {11, 16, 24, 32, 41, 50, 58, 63, 68, 72, 77, 82};
	private final boolean _percent;
	
	public EffectDamOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
		_percent = getTemplate().getParam().getBool("percent", false);
	}
	
	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead())
		{
			return false;
		}
		double damage = calc();
		if(_percent)
		{
			damage = (double) _effected.getMaxHp() * _template._value * 0.01;
		}
		if(damage < 2.0 && getStackOrder() != -1)
		{
			switch(getEffectType())
			{
				case Poison:
				{
					damage = (long) poison[getStackOrder() - 1] * getPeriod() / 1000;
					break;
				}
				case Bleed:
				{
					damage = (long) bleed[getStackOrder() - 1] * getPeriod() / 1000;
				}
			}
		}
		if((damage = _effector.calcStat(getSkill().isMagic() ? Stats.MAGIC_DAMAGE : Stats.PHYSICAL_DAMAGE, damage, _effected, getSkill())) > _effected.getCurrentHp() - 1.0 && !_effected.isNpc())
		{
			if(!getSkill().isOffensive())
			{
				_effected.sendPacket(Msg.NOT_ENOUGH_HP);
			}
			return false;
		}
		if(getSkill().getAbsorbPart() > 0.0)
		{
			_effector.setCurrentHp(getSkill().getAbsorbPart() * Math.min(_effected.getCurrentHp(), damage) + _effector.getCurrentHp(), false);
		}
		_effected.reduceCurrentHp(damage, _effector, getSkill(), !_effected.isNpc() && _effected != _effector, _effected != _effector, _effector.isNpc() || _effected == _effector, false, false, true, false);
		return true;
	}
}