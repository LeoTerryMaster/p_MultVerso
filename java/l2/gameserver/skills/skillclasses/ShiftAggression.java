package l2.gameserver.skills.skillclasses;

import l2.gameserver.model.AggroList;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.World;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class ShiftAggression extends Skill
{
	public ShiftAggression(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		if(activeChar.getPlayer() == null)
		{
			return;
		}
		for(Creature target : targets)
		{
			if(target == null || !target.isPlayer())
				continue;
			Player player = (Player) target;
			for(NpcInstance npc : World.getAroundNpc(activeChar, getSkillRadius(), getSkillRadius()))
			{
				AggroList.AggroInfo ai = npc.getAggroList().get(activeChar);
				if(ai == null)
					continue;
				npc.getAggroList().addDamageHate(player, 0, ai.hate);
				npc.getAggroList().remove(activeChar, true);
			}
		}
		if(isSSPossible())
		{
			activeChar.unChargeShots(isMagic());
		}
	}
}