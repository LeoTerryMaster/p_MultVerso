package ai.isle_of_prayer;

import l2.commons.util.Rnd;
import l2.gameserver.ai.Fighter;
import l2.gameserver.model.Creature;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;

public class Shade extends Fighter
{
	private static final int DESPAWN_TIME = 300000;
	private static final int BLUE_CRYSTAL = 9595;
	private long _wait_timeout;
	private boolean _wait;
	
	public Shade(NpcInstance actor)
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
			_wait_timeout = System.currentTimeMillis() + 300000;
		}
		if(_wait_timeout != 0 && _wait && _wait_timeout < System.currentTimeMillis())
		{
			actor.deleteMe();
			return true;
		}
		return super.thinkActive();
	}
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		Player player;
		if(killer != null && (player = killer.getPlayer()) != null)
		{
			NpcInstance actor = getActor();
			if(Rnd.chance(10))
			{
				actor.dropItem(player, 9595, 1);
			}
		}
		super.onEvtDead(killer);
	}
}