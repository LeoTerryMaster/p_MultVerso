package l2.gameserver.skills.effects;

import l2.gameserver.model.Effect;
import l2.gameserver.model.Summon;
import l2.gameserver.stats.Env;

public final class EffectDestroySummon extends Effect
{
	public EffectDestroySummon(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean checkCondition()
	{
		if(!_effected.isSummon())
		{
			return false;
		}
		return super.checkCondition();
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		((Summon) _effected).unSummon();
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}