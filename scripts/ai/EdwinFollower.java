package ai;

import l2.commons.lang.reference.HardReference;
import l2.commons.lang.reference.HardReferences;
import l2.commons.util.Rnd;
import l2.gameserver.ai.DefaultAI;
import l2.gameserver.model.Creature;
import l2.gameserver.model.World;
import l2.gameserver.model.instances.NpcInstance;

public class EdwinFollower extends DefaultAI
{
	private static final int EDWIN_ID = 32072;
	private static final int DRIFT_DISTANCE = 200;
	private long _wait_timeout;
	private HardReference<? extends Creature> _edwinRef = HardReferences.emptyRef();
	
	public EdwinFollower(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
	
	@Override
	protected boolean randomAnimation()
	{
		return false;
	}
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}
	
	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		Creature edwin = _edwinRef.get();
		if(edwin == null)
		{
			if(System.currentTimeMillis() > _wait_timeout)
			{
				_wait_timeout = System.currentTimeMillis() + 15000;
				for(NpcInstance npc : World.getAroundNpc(actor))
				{
					if(npc.getNpcId() != 32072)
						continue;
					_edwinRef = npc.getRef();
					return true;
				}
			}
		}
		else if(!actor.isMoving())
		{
			int x = edwin.getX() + Rnd.get(400) - 200;
			int y = edwin.getY() + Rnd.get(400) - 200;
			int z = edwin.getZ();
			actor.setRunning();
			actor.moveToLocation(x, y, z, 0, true);
			return true;
		}
		return false;
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
	}
	
	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
	}
}