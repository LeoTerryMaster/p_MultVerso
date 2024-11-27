package l2.gameserver.skills.effects;

import l2.gameserver.model.Effect;
import l2.gameserver.stats.Env;

public final class EffectBuffImmunity extends Effect
{
	public EffectBuffImmunity(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		getEffected().startBuffImmunity();
	}
	
	@Override
	public void onExit()
	{
		super.onExit();
		getEffected().stopBuffImmunity();
	}
	
	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead())
		{
			return false;
		}
		return getSkill().isToggle();
	}
}