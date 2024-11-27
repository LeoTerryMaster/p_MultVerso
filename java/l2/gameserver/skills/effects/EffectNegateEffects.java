package l2.gameserver.skills.effects;

import l2.gameserver.model.Effect;
import l2.gameserver.stats.Env;

public class EffectNegateEffects extends Effect
{
	public EffectNegateEffects(Env env, EffectTemplate template)
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
			if((e.getStackType().equals("none") || !e.getStackType().equals(getStackType()) && !e.getStackType().equals(getStackType2())) && (e.getStackType2().equals("none") || !e.getStackType2().equals(getStackType()) && !e.getStackType2().equals(getStackType2())) || e.getStackOrder() > getStackOrder())
				continue;
			e.exit();
		}
		return false;
	}
}