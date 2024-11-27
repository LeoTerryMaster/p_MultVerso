package ai;

import l2.commons.util.Rnd;
import l2.gameserver.ai.DefaultAI;
import l2.gameserver.geodata.GeoEngine;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.utils.Location;

public class RndWalkAndAnim extends DefaultAI
{
	protected static final int PET_WALK_RANGE = 100;
	
	public RndWalkAndAnim(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor.isMoving())
		{
			return false;
		}
		int val = Rnd.get(100);
		if(val < 10)
		{
			randomWalk();
		}
		else if(val < 20)
		{
			actor.onRandomAnimation();
		}
		return false;
	}
	
	@Override
	protected boolean randomWalk()
	{
		NpcInstance actor = getActor();
		if(actor == null)
		{
			return false;
		}
		Location sloc = actor.getSpawnedLoc();
		int x = sloc.x + Rnd.get(200) - 100;
		int y = sloc.y + Rnd.get(200) - 100;
		int z = GeoEngine.getHeight(x, y, sloc.z, actor.getGeoIndex());
		actor.setRunning();
		actor.moveToLocation(x, y, z, 0, true);
		return true;
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