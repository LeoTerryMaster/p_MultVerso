package ai.custom;

import l2.commons.threading.RunnableImpl;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.ai.Fighter;
import l2.gameserver.model.instances.NpcInstance;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class SSQLilithMinion extends Fighter
{
	private final int[] _enemies = {32719, 32720, 32721};
	
	public SSQLilithMinion(NpcInstance actor)
	{
		super(actor);
		actor.setHasChatWindow(false);
	}
	
	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		ThreadPoolManager.getInstance().schedule(new Attack(), 3000);
	}
	
	private NpcInstance getEnemy()
	{
		List<NpcInstance> around = getActor().getAroundNpc(1000, 300);
		if(around != null && !around.isEmpty())
		{
			for(NpcInstance npc : around)
			{
				if(!ArrayUtils.contains(_enemies, npc.getNpcId()))
					continue;
				return npc;
			}
		}
		return null;
	}
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}
	
	public class Attack extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			if(getEnemy() != null)
			{
				getActor().getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, getEnemy(), 10000000);
			}
		}
	}
}