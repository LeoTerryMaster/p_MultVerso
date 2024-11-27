package l2.gameserver.stats.conditions;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.stats.Env;

public class ConditionTargetInTheSameParty extends Condition
{
	private final boolean _val;
	
	public ConditionTargetInTheSameParty(boolean val)
	{
		_val = val;
	}
	
	@Override
	protected boolean testImpl(Env env)
	{
		Creature creature = env.character;
		Creature targetCreature = env.target;
		if(!creature.isPlayable() || targetCreature == null || !targetCreature.isPlayable())
		{
			return !_val;
		}
		Player player = creature.getPlayer();
		Player target;
		if(player == (target = targetCreature.getPlayer()))
		{
			return _val;
		}
		if(player.isInParty() && player.getParty() == target.getParty())
		{
			return _val;
		}
		return !_val;
	}
}