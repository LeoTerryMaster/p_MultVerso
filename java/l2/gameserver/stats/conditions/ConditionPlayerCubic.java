package l2.gameserver.stats.conditions;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Player;
import l2.gameserver.stats.Env;
import l2.gameserver.stats.Stats;

public class ConditionPlayerCubic extends Condition
{
	private final int _id;
	
	public ConditionPlayerCubic(int id)
	{
		_id = id;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		if(env.target == null || !env.target.isPlayer())
		{
			return false;
		}
		Player targetPlayer = (Player) env.target;
		if(targetPlayer.getCubic(_id) != null)
		{
			return true;
		}
		int size = (int) targetPlayer.calcStat(Stats.CUBICS_LIMIT, 1.0);
		if(targetPlayer.getCubics().size() >= size)
		{
			if(env.character == targetPlayer)
			{
				targetPlayer.sendPacket(Msg.CUBIC_SUMMONING_FAILED);
			}
			return false;
		}
		return true;
	}
}