package ai;

import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.ai.DefaultAI;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;

import java.util.List;

public class FieldMachine extends DefaultAI
{
	private long _lastAction;
	
	public FieldMachine(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(attacker == null || attacker.getPlayer() == null)
		{
			return;
		}
		if(System.currentTimeMillis() - _lastAction > 15000)
		{
			_lastAction = System.currentTimeMillis();
			Functions.npcSayCustomMessage(actor, "scripts.ai.FieldMachine." + actor.getNpcId(), (Object[]) new Object[0]);
			List<NpcInstance> around = actor.getAroundNpc(1500, 300);
			if(around != null && !around.isEmpty())
			{
				for(NpcInstance npc : around)
				{
					if(!npc.isMonster() || npc.getNpcId() < 22656 || npc.getNpcId() > 22659)
						continue;
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 5000);
				}
			}
		}
	}
}