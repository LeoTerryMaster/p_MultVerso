package l2.gameserver.skills.effects;

import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.model.Effect;
import l2.gameserver.stats.Env;

public class EffectHate extends Effect
{
	public EffectHate(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		if(_effected.isNpc() && _effected.isMonster())
		{
			_effected.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, _effector, _template._value);
		}
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