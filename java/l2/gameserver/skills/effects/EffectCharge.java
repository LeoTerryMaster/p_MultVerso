package l2.gameserver.skills.effects;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Effect;
import l2.gameserver.model.Player;
import l2.gameserver.stats.Env;

public final class EffectCharge extends Effect
{
	public static final int MAX_CHARGE = 7;
	private final int _charges;
	private final boolean _fullCharge;
	
	public EffectCharge(Env env, EffectTemplate template)
	{
		super(env, template);
		_charges = template.getParam().getInteger("charges", 7);
		_fullCharge = template.getParam().getBool("fullCharge", false);
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		if(getEffected().isPlayer())
		{
			Player player = (Player) getEffected();
			if(player.getIncreasedForce() >= _charges)
			{
				player.sendPacket(Msg.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY);
			}
			else if(_fullCharge)
			{
				player.setIncreasedForce(_charges);
			}
			else
			{
				player.setIncreasedForce(player.getIncreasedForce() + 1);
			}
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}