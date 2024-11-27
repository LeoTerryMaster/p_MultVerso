package l2.gameserver.skills.skillclasses;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class PcBangPointsAdd extends Skill
{
	public PcBangPointsAdd(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		int points = (int) _power;
		for(Creature target : targets)
		{
			if(target.isPlayer())
			{
				Player player = target.getPlayer();
				player.addPcBangPoints(points, false);
			}
			getEffects(activeChar, target, getActivateRate() > 0, false);
		}
		if(isSSPossible())
		{
			activeChar.unChargeShots(isMagic());
		}
	}
}