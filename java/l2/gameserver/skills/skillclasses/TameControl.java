package l2.gameserver.skills.skillclasses;

import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.Skill;
import l2.gameserver.model.instances.TamedBeastInstance;
import l2.gameserver.templates.StatsSet;

import java.util.List;

public class TameControl extends Skill
{
	private final int _type;
	
	public TameControl(StatsSet set)
	{
		super(set);
		_type = set.getInteger("type", 0);
	}
	
	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		if(isSSPossible())
		{
			activeChar.unChargeShots(isMagic());
		}
		if(!activeChar.isPlayer())
		{
			return;
		}
		Player player = activeChar.getPlayer();
		if(player.getTrainedBeast() == null)
		{
			return;
		}
		TamedBeastInstance tamedBeast;
		if(_type == 0)
		{
			for(Creature target : targets)
			{
				if(target == null || !(target instanceof TamedBeastInstance) || player.getTrainedBeast() != target)
					continue;
				player.getTrainedBeast().despawnWithDelay(1000);
			}
		}
		else if(_type > 0 && (tamedBeast = player.getTrainedBeast()) != null)
		{
			switch(_type)
			{
				case 1:
				{
					tamedBeast.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player);
					break;
				}
				case 3:
				{
					tamedBeast.buffOwner();
					break;
				}
				case 4:
				{
					tamedBeast.doDespawn();
				}
			}
		}
	}
}