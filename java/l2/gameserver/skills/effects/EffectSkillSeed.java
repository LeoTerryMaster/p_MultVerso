package l2.gameserver.skills.effects;

import l2.gameserver.model.Effect;
import l2.gameserver.stats.Env;

public final class EffectSkillSeed extends Effect
{
	private int _seeds = 1;
	
	public EffectSkillSeed(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	public void incSeeds()
	{
		++_seeds;
	}
	
	public int getSeeds()
	{
		return _seeds;
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}