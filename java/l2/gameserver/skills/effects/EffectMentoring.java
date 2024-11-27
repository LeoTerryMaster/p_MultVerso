package l2.gameserver.skills.effects;

import l2.gameserver.Config;
import l2.gameserver.model.Effect;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.Mentoring;
import l2.gameserver.stats.Env;

public class EffectMentoring extends Effect
{
	private final MentoringTargetType _target_type;
	
	public EffectMentoring(Env env, EffectTemplate template)
	{
		super(env, template);
		_target_type = template.getParam().getEnum("MentoringTarget", MentoringTargetType.class);
	}
	
	private boolean applycable()
	{
		Player player = getEffected().getPlayer();
		if(Config.MENTORING_ENABLE && player != null && player.getMentoring() != null)
		{
			Mentoring m = player.getMentoring();
			if(_target_type == MentoringTargetType.MENTOR && m.isMentor(player) && m.isMentorBuffApplicable(player) || _target_type == MentoringTargetType.MENTEE && m.isMentee(player) && m.isMenteeBuffApplicable(player))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean checkCondition()
	{
		if(getEffected() != null && getEffected().getEffectList().getEffectsBySkill(getSkill()) == null)
		{
			return applycable();
		}
		return false;
	}
	
	@Override
	protected boolean onActionTime()
	{
		return applycable();
	}
	
	@Override
	public void exit()
	{
		if(!applycable())
		{
			super.exit();
		}
	}
	
	private enum MentoringTargetType
	{
		MENTOR,
		MENTEE;
	}
}