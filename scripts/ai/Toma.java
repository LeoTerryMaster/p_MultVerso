package ai;

import l2.commons.util.Rnd;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.DefaultAI;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.MagicSkillUse;
import l2.gameserver.utils.Location;

public class Toma extends DefaultAI
{
	private static final long TELEPORT_PERIOD = 1800000;
	private final Location[] _points = {new Location(151680, -174891, -1807, 41400), new Location(154153, -220105, -3402), new Location(178834, -184336, -352)};
	private long _lastTeleport = System.currentTimeMillis();
	
	public Toma(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected boolean thinkActive()
	{
		if(System.currentTimeMillis() - _lastTeleport < TELEPORT_PERIOD)
		{
			return false;
		}
		NpcInstance _thisActor = getActor();
		Location loc = _points[Rnd.get(_points.length)];
		if(_thisActor.getLoc().equals(loc))
		{
			return false;
		}
		_thisActor.broadcastPacketToOthers(new MagicSkillUse(_thisActor, _thisActor, 4671, 1, 1000, 0));
		ThreadPoolManager.getInstance().schedule(new Teleport(loc), 1000);
		_lastTeleport = System.currentTimeMillis();
		return true;
	}
	
	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
}