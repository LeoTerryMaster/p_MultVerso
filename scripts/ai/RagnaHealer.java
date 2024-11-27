package ai;

import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.ai.Priest;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;

import java.util.List;

public class RagnaHealer extends Priest
{
	private long lastFactionNotifyTime;
	
	public RagnaHealer(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(attacker == null)
		{
			return;
		}
		if(System.currentTimeMillis() - lastFactionNotifyTime > 10000)
		{
			lastFactionNotifyTime = System.currentTimeMillis();
			List<NpcInstance> around = actor.getAroundNpc(500, 300);
			if(around != null && !around.isEmpty())
			{
				for(NpcInstance npc : around)
				{
					if(!npc.isMonster() || npc.getNpcId() < 22691 || npc.getNpcId() > 22702)
						continue;
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 5000);
				}
			}
		}
		super.onEvtAttacked(attacker, damage);
	}
}