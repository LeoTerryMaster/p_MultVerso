package l2.gameserver.skills.effects;

import l2.gameserver.model.Effect;
import l2.gameserver.network.l2.s2c.FinishRotating;
import l2.gameserver.network.l2.s2c.StartRotating;
import l2.gameserver.stats.Env;

public final class EffectBluff extends Effect
{
	public EffectBluff(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean checkCondition()
	{
		if(getEffected().isNpc() && !getEffected().isMonster())
		{
			return false;
		}
		return super.checkCondition();
	}
	
	@Override
	public void onStart()
	{
		getEffected().broadcastPacket(new StartRotating(getEffected(), getEffected().getHeading(), 1, 65535));
		getEffected().broadcastPacket(new FinishRotating(getEffected(), getEffector().getHeading(), 65535));
		getEffected().setHeading(getEffector().getHeading());
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