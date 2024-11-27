package ai;

import l2.commons.threading.RunnableImpl;
import l2.commons.util.Rnd;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.DefaultAI;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;

public class Furance extends DefaultAI
{
	public Furance(NpcInstance actor)
	{
		super(actor);
		actor.startImmobilized();
	}
	
	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		NpcInstance actor = getActor();
		if(Rnd.chance(50))
		{
			actor.setNpcState(1);
		}
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new Switch(), 300000, 300000);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
	}
	
	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
	}
	
	@Override
	protected boolean randomAnimation()
	{
		return false;
	}
	
	@Override
	public boolean isGlobalAI()
	{
		return true;
	}
	
	public class Switch extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			NpcInstance actor = getActor();
			if(actor.getNpcState() == 1)
			{
				actor.setNpcState(2);
			}
			else
			{
				actor.setNpcState(1);
			}
		}
	}
}