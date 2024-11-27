package l2.gameserver.skills.skillclasses;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.World;
import l2.gameserver.model.instances.TrapInstance;
import l2.gameserver.network.l2.s2c.NpcInfo;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class DetectTrap extends Skill
{
	public DetectTrap(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
		{
			TrapInstance trap;
			if(target == null || !target.isTrap() || (double) (trap = (TrapInstance) target).getLevel() > getPower())
				continue;
			trap.setDetected(true);
			for(Player player : World.getAroundPlayers(trap))
			{
				player.sendPacket(new NpcInfo(trap, player));
			}
		}
		if(isSSPossible())
		{
			activeChar.unChargeShots(isMagic());
		}
	}
}