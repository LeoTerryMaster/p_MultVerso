package ai;

import l2.gameserver.ai.DefaultAI;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.Earthquake;

import java.util.List;

public class BaiumNpc extends DefaultAI
{
	private static final int BAIUM_EARTHQUAKE_TIMEOUT = 900000;
	private long _wait_timeout;
	
	public BaiumNpc(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
	
	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(_wait_timeout < System.currentTimeMillis())
		{
			_wait_timeout = System.currentTimeMillis() + 900000;
			Earthquake eq = new Earthquake(actor.getLoc(), 40, 10);
			List<Creature> chars = actor.getAroundCharacters(5000, 10000);
			for(Creature character : chars)
			{
				if(!character.isPlayer())
					continue;
				character.sendPacket(eq);
			}
		}
		return false;
	}
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}