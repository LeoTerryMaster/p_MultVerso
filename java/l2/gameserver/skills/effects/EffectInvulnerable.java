package l2.gameserver.skills.effects;

import l2.gameserver.model.Effect;
import l2.gameserver.model.Skill;
import l2.gameserver.stats.Env;

public final class EffectInvulnerable extends Effect
{
	public EffectInvulnerable(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean checkCondition()
	{
		if(_effected.isInvul())
		{
			return false;
		}
		Skill skill = _effected.getCastingSkill();
		if(skill != null && skill.getSkillType() == Skill.SkillType.TAKECASTLE)
		{
			return false;
		}
		return super.checkCondition();
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		_effected.startHealBlocked();
		_effected.setIsInvul(true);
	}
	
	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopHealBlocked();
		_effected.setIsInvul(false);
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}