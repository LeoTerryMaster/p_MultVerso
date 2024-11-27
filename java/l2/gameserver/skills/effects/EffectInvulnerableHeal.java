package l2.gameserver.skills.effects;

import l2.gameserver.model.Effect;
import l2.gameserver.model.Skill;
import l2.gameserver.stats.Env;

public final class EffectInvulnerableHeal extends Effect
{
	public EffectInvulnerableHeal(Env env, EffectTemplate template)
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
		_effected.setIsInvul(true);
	}
	
	@Override
	public void onExit()
	{
		super.onExit();
		_effected.setIsInvul(false);
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}