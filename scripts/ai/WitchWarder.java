package ai;

import l2.gameserver.ai.Fighter;
import l2.gameserver.model.instances.NpcInstance;

public class WitchWarder extends Fighter
{
	private static final int DESPAWN_TIME = 180000;
	private long _wait_timeout;
	private boolean _wait;
	
	public WitchWarder(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
		{
			return true;
		}
		if(_def_think)
		{
			doTask();
			_wait = false;
			return true;
		}
		if(!_wait)
		{
			_wait = true;
			_wait_timeout = System.currentTimeMillis() + 180000;
		}
		if(_wait_timeout != 0 && _wait && _wait_timeout < System.currentTimeMillis())
		{
			actor.deleteMe();
		}
		return super.thinkActive();
	}
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}