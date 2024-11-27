package ai;

import l2.commons.util.Rnd;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.ai.Fighter;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;

public class MoSMonk extends Fighter
{
	public MoSMonk(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onIntentionAttack(Creature target)
	{
		NpcInstance actor = getActor();
		if(getIntention() == CtrlIntention.AI_INTENTION_ACTIVE && Rnd.chance(20))
		{
			Functions.npcSayCustomMessage(getActor(), "scripts.ai.MoSMonk.onIntentionAttack", (Object[]) new Object[0]);
		}
		super.onIntentionAttack(target);
	}
	
	@Override
	public boolean checkAggression(Creature target)
	{
		if(target.getActiveWeaponInstance() == null)
		{
			return false;
		}
		return super.checkAggression(target);
	}
}