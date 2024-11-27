package ai.isle_of_prayer;

import l2.commons.util.Rnd;
import l2.gameserver.ai.Fighter;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;

public class WaterDragonDetractor extends Fighter
{
	private static final int SPIRIT_OF_LAKE = 9689;
	private static final int BLUE_CRYSTAL = 9595;
	
	public WaterDragonDetractor(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		Player player;
		if(killer != null && (player = killer.getPlayer()) != null)
		{
			NpcInstance actor = getActor();
			actor.dropItem(player, 9689, 1);
			if(Rnd.chance(10))
			{
				actor.dropItem(player, 9595, 1);
			}
		}
		super.onEvtDead(killer);
	}
}