package ai;

import l2.commons.util.Rnd;
import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.ai.Fighter;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.scripts.Functions;

public class TimakOrcTroopLeader extends Fighter
{
	private static final int[] BROTHERS = {20768, 20769, 20770};
	private boolean _firstTimeAttacked = true;
	
	public TimakOrcTroopLeader(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(!actor.isDead() && _firstTimeAttacked)
		{
			_firstTimeAttacked = false;
			Functions.npcSay(actor, "Show yourselves!");
			for(int bro : BROTHERS)
			{
				try
				{
					NpcInstance npc = NpcHolder.getInstance().getTemplate(bro).getNewInstance();
					npc.setSpawnedLoc(((MonsterInstance) actor).getMinionPosition());
					npc.setReflection(actor.getReflection());
					npc.setCurrentHpMp((double) npc.getMaxHp(), (double) npc.getMaxMp(), true);
					npc.spawnMe(npc.getSpawnedLoc());
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Rnd.get(1, 100));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		super.onEvtAttacked(attacker, damage);
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		_firstTimeAttacked = true;
		super.onEvtDead(killer);
	}
}