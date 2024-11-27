package l2.gameserver.skills.effects;

import l2.gameserver.model.Effect;
import l2.gameserver.stats.Env;

public class EffectNegateMusic extends Effect
{
	public EffectNegateMusic(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
	}
	
	@Override
	public void onExit()
	{
		super.onExit();
	}
	
	@Override
	public boolean onActionTime()
	{
		for(Effect e : _effected.getEffectList().getAllEffects())
		{
			if(!e.getSkill().isMusic())
				continue;
			e.exit();
		}
		return false;
	}
}