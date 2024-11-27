package l2.gameserver.skills.effects;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Effect;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.stats.Env;

public class EffectLDManaDamOverTime extends Effect
{
	public EffectLDManaDamOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead())
		{
			return false;
		}
		double manaDam = calc();
		if((manaDam *= (double) _effected.getLevel() / 2.4) > _effected.getCurrentMp() && getSkill().isToggle())
		{
			_effected.sendPacket(Msg.NOT_ENOUGH_MP);
			_effected.sendPacket(new SystemMessage(749).addSkillName(getSkill().getId(), getSkill().getDisplayLevel()));
			return false;
		}
		_effected.reduceCurrentMp(manaDam, null);
		return true;
	}
}