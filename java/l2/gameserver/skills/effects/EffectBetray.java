package l2.gameserver.skills.effects;

import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.model.Effect;
import l2.gameserver.model.Summon;
import l2.gameserver.stats.Env;

public class EffectBetray extends Effect
{
	public EffectBetray(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		if(_effected != null && _effected.isSummon())
		{
			Summon summon = (Summon) _effected;
			summon.setDepressed(true);
			summon.getAI().Attack(summon.getPlayer(), true, false);
		}
	}
	
	@Override
	public void onExit()
	{
		super.onExit();
		if(_effected != null && _effected.isSummon())
		{
			Summon summon = (Summon) _effected;
			summon.setDepressed(false);
			summon.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}