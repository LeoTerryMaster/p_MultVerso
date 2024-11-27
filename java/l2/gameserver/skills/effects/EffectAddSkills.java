package l2.gameserver.skills.effects;

import l2.gameserver.model.Effect;
import l2.gameserver.model.Skill;
import l2.gameserver.stats.Env;

public class EffectAddSkills extends Effect
{
	public EffectAddSkills(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		for(Skill.AddedSkill as : getSkill().getAddedSkills())
		{
			getEffected().addSkill(as.getSkill());
		}
	}
	
	@Override
	public void onExit()
	{
		super.onExit();
		for(Skill.AddedSkill as : getSkill().getAddedSkills())
		{
			getEffected().removeSkill(as.getSkill());
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}