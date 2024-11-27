package l2.gameserver.skills.effects;

import l2.gameserver.model.Effect;
import l2.gameserver.model.Skill;
import l2.gameserver.stats.Env;

public class EffectMutePhisycal extends Effect
{
	public EffectMutePhisycal(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		Skill castingSkill;
		if(!_effected.startPMuted() && (castingSkill = _effected.getCastingSkill()) != null && !castingSkill.isMagic())
		{
			_effected.abortCast(true, true);
		}
	}
	
	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopPMuted();
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}