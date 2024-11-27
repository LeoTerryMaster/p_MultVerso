package l2.gameserver.skills.effects;

import l2.commons.util.Rnd;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.ai.DefaultAI;
import l2.gameserver.model.Effect;
import l2.gameserver.stats.Env;

public final class EffectRemoveTarget extends Effect
{
	private final boolean _doStopTarget;
	
	public EffectRemoveTarget(Env env, EffectTemplate template)
	{
		super(env, template);
		_doStopTarget = template.getParam().getBool("doStopTarget", false);
	}
	
	@Override
	public boolean checkCondition()
	{
		return Rnd.chance(_template.chance(100));
	}
	
	@Override
	public void onStart()
	{
		if(getEffected().getAI() instanceof DefaultAI)
		{
			((DefaultAI) getEffected().getAI()).setGlobalAggro(System.currentTimeMillis() + 3000);
		}
		getEffected().setTarget(null);
		if(_doStopTarget)
		{
			getEffected().stopMove();
		}
		getEffected().abortAttack(true, true);
		getEffected().abortCast(true, true);
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, getEffector());
	}
	
	@Override
	public boolean isHidden()
	{
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}