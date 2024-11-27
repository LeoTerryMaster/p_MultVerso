package ai;

import l2.commons.util.Rnd;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.Fighter;
import l2.gameserver.geodata.GeoEngine;
import l2.gameserver.model.Territory;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.templates.spawn.SpawnRange;
import l2.gameserver.utils.Location;

public class RndTeleportFighter extends Fighter
{
	private long _lastTeleport;
	
	public RndTeleportFighter(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected boolean maybeMoveToHome()
	{
		NpcInstance actor = getActor();
		if(System.currentTimeMillis() - _lastTeleport < 10000)
		{
			return false;
		}
		boolean randomWalk = actor.hasRandomWalk();
		Location sloc = actor.getSpawnedLoc();
		if(sloc == null)
		{
			return false;
		}
		if(randomWalk && (!Config.RND_WALK || Rnd.chance(Config.RND_WALK_RATE)))
		{
			return false;
		}
		if(!randomWalk && actor.isInRangeZ(sloc, (long) Config.MAX_DRIFT_RANGE))
		{
			return false;
		}
		int x = sloc.x + Rnd.get(-Config.MAX_DRIFT_RANGE, Config.MAX_DRIFT_RANGE);
		int y;
		int z = GeoEngine.getHeight(x, y = sloc.y + Rnd.get(-Config.MAX_DRIFT_RANGE, Config.MAX_DRIFT_RANGE), sloc.z, actor.getGeoIndex());
		if(sloc.z - z > 64)
		{
			return false;
		}
		SpawnRange spawnRange = actor.getSpawnRange();
		boolean isInside = true;
		if(spawnRange != null && spawnRange instanceof Territory)
		{
			isInside = ((Territory) spawnRange).isInside(x, y);
		}
		if(isInside)
		{
			actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 4671, 1, 500, 0));
			ThreadPoolManager.getInstance().schedule(new Teleport(new Location(x, y, z)), 500);
			_lastTeleport = System.currentTimeMillis();
		}
		return isInside;
	}
}