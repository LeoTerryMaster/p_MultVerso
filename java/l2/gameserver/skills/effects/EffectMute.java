package l2.gameserver.skills.effects;

import l2.gameserver.model.Effect;
import l2.gameserver.model.Skill;
import l2.gameserver.stats.Env;

public class EffectMute extends Effect
{
	public EffectMute(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		Skill castingSkill;
		if(!_effected.startMuted() && (castingSkill = _effected.getCastingSkill()) != null && castingSkill.isMagic())
		{
			_effected.abortCast(true, true);
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
	
	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopMuted();
	}
}