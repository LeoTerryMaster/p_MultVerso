package l2.gameserver.skills.skillclasses;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class Disablers extends Skill
{
	private final boolean _skillInterrupt;
	
	public Disablers(StatsSet set)
	{
		super(set);
		_skillInterrupt = set.getBool("skillInterrupt", false);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
		{
			if(target == null)
				continue;
			boolean reflected = target.checkReflectSkill(activeChar, this);
			Creature realTarget;
			Creature creature = realTarget = reflected ? activeChar : target;
			if(_skillInterrupt)
			{
				if(realTarget.getCastingSkill() != null && !realTarget.getCastingSkill().isMagic() && !realTarget.isRaid())
				{
					realTarget.abortCast(false, true);
				}
				if(!realTarget.isRaid())
				{
					realTarget.abortAttack(true, true);
				}
			}
			getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
		}
		if(isSSPossible())
		{
			activeChar.unChargeShots(isMagic());
		}
	}
}