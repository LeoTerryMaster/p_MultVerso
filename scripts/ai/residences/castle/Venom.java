package ai.residences.castle;

import l2.gameserver.ai.Fighter;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;
import l2.gameserver.utils.NpcUtils;

public class Venom extends Fighter
{
	public Venom(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	public void onEvtSpawn()
	{
		super.onEvtSpawn();
		Functions.npcShout(getActor(), "Who dares to covet the throne of our castle!  Leave immediately or you will pay the price of your audacity with your very own blood!");
	}
	
	@Override
	public void onEvtDead(Creature killer)
	{
		super.onEvtDead(killer);
		Functions.npcShout(getActor(), "It's not over yet...  It won't be... over... like this... Never...");
		NpcUtils.spawnSingle(29055, 12589, -49044, -3008, (long) 120000);
	}
}