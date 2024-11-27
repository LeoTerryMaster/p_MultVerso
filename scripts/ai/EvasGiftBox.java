package ai;

import l2.commons.util.Rnd;
import l2.gameserver.ai.Fighter;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;

public class EvasGiftBox extends Fighter
{
	private static final int[] KISS_OF_EVA = {1073, 3141, 3252};
	private static final int Red_Coral = 9692;
	private static final int Crystal_Fragment = 9693;
	
	public EvasGiftBox(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		Player player;
		NpcInstance actor = getActor();
		if(killer != null && (player = killer.getPlayer()) != null && player.getEffectList().containEffectFromSkills(KISS_OF_EVA))
		{
			actor.dropItem(player, Rnd.chance(50) ? 9692 : 9693, 1);
		}
		super.onEvtDead(killer);
	}
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}