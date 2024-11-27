package l2.gameserver.stats.triggers;

import l2.commons.lang.ArrayUtils;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.stats.Env;
import l2.gameserver.stats.conditions.Condition;

public class TriggerInfo extends Skill.AddedSkill
{
	private final TriggerType _type;
	private final double _chance;
	private Condition[] _conditions = Condition.EMPTY_ARRAY;
	
	public TriggerInfo(int id, int level, TriggerType type, double chance)
	{
		super(id, level);
		_type = type;
		_chance = chance;
	}
	
	public final void addCondition(Condition c)
	{
		_conditions = ArrayUtils.add(_conditions, c);
	}
	
	public boolean checkCondition(Creature actor, Creature target, Creature aimTarget, Skill owner, double damage)
	{
		if(getSkill().checkTarget(actor, aimTarget, aimTarget, false, false) != null)
		{
			return false;
		}
		Env env = new Env();
		env.character = actor;
		env.skill = owner;
		env.target = target;
		env.value = damage;
		for(Condition c : _conditions)
		{
			if(c.test(env))
				continue;
			return false;
		}
		return true;
	}
	
	public TriggerType getType()
	{
		return _type;
	}
	
	public double getChance()
	{
		return _chance;
	}
}