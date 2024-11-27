package ai;

import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.ai.Fighter;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;

import java.util.List;

public class KanadisFollower extends Fighter
{
	public KanadisFollower(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		NpcInstance actor = getActor();
		List<NpcInstance> around = actor.getAroundNpc(7000, 300);
		if(around != null && !around.isEmpty())
		{
			for(NpcInstance npc : around)
			{
				if(npc.getNpcId() != 36562)
					continue;
				actor.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, npc, 500);
			}
		}
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(attacker.getNpcId() == 36562)
		{
			actor.getAggroList().addDamageHate(attacker, 0, 100);
			startRunningTask(2000);
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}
		super.onEvtAttacked(attacker, damage);
	}
	
	@Override
	protected boolean maybeMoveToHome()
	{
		return false;
	}
}