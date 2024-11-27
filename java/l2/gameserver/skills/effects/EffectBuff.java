package l2.gameserver.skills.effects;

import l2.gameserver.model.Effect;
import l2.gameserver.stats.Env;

public final class EffectBuff extends Effect
{
	public EffectBuff(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}