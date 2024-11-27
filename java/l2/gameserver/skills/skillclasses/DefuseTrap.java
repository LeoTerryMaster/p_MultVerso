package l2.gameserver.skills.skillclasses;

import l2.gameserver.cache.Msg;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.TrapInstance;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class DefuseTrap extends Skill
{
	public DefuseTrap(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(target == null || !target.isTrap())
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return false;
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
		{
			TrapInstance trap;
			if(target == null || !target.isTrap() || (double) (trap = (TrapInstance) target).getLevel() > getPower())
				continue;
			trap.deleteMe();
		}
		if(isSSPossible())
		{
			activeChar.unChargeShots(isMagic());
		}
	}
}