package l2.gameserver.skills.skillclasses;

import l2.commons.util.Rnd;
import l2.gameserver.cache.Msg;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.ChestInstance;
import l2.gameserver.model.instances.DoorInstance;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class Unlock extends Skill
{
	private final int _unlockPower;
	
	public Unlock(StatsSet set)
	{
		super(set);
		_unlockPower = set.getInteger("unlockPower", 0) + 100;
	}
	
	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(target == null || target instanceof ChestInstance && target.isDead())
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return false;
		}
		if(target instanceof ChestInstance && activeChar.isPlayer())
		{
			return super.checkCondition(activeChar, target, forceUse, dontMove, first);
		}
		if(!target.isDoor() || _unlockPower == 0)
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return false;
		}
		DoorInstance door = (DoorInstance) target;
		if(door.isOpen())
		{
			activeChar.sendPacket(Msg.IT_IS_NOT_LOCKED);
			return false;
		}
		if(!door.isUnlockable())
		{
			activeChar.sendPacket(Msg.YOU_ARE_UNABLE_TO_UNLOCK_THE_DOOR);
			return false;
		}
		if(door.getKey() > 0)
		{
			activeChar.sendPacket(Msg.YOU_ARE_UNABLE_TO_UNLOCK_THE_DOOR);
			return false;
		}
		if(_unlockPower - door.getLevel() * 100 < 0)
		{
			activeChar.sendPacket(Msg.YOU_ARE_UNABLE_TO_UNLOCK_THE_DOOR);
			return false;
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature targ : targets)
		{
			if(targ == null)
				continue;
			if(targ.isDoor())
			{
				DoorInstance target = (DoorInstance) targ;
				if(!target.isOpen() && (target.getKey() > 0 || Rnd.chance(_unlockPower - target.getLevel() * 100)))
				{
					target.openMe((Player) activeChar, true);
					continue;
				}
				activeChar.sendPacket(Msg.YOU_HAVE_FAILED_TO_UNLOCK_THE_DOOR);
				continue;
			}
			ChestInstance target;
			if(!(targ instanceof ChestInstance) || (target = (ChestInstance) targ).isDead())
				continue;
			target.tryOpen((Player) activeChar, this);
		}
	}
}