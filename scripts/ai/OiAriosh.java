package ai;

import l2.gameserver.ai.CtrlEvent;
import l2.gameserver.ai.Fighter;
import l2.gameserver.data.xml.holder.NpcHolder;
import l2.gameserver.model.Creature;
import l2.gameserver.model.SimpleSpawner;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.utils.Location;

public class OiAriosh extends Fighter
{
	private static final int MOB = 18556;
	private static final int[] _hps = {80, 60, 40, 30, 20, 10, 5, -5};
	private int _hpCount;
	
	public OiAriosh(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(!actor.isDead() && actor.getCurrentHpPercents() < (double) _hps[_hpCount])
		{
			spawnMob(attacker);
			++_hpCount;
		}
		super.onEvtAttacked(attacker, damage);
	}
	
	private void spawnMob(Creature attacker)
	{
		NpcInstance actor = getActor();
		try
		{
			SimpleSpawner sp = new SimpleSpawner(NpcHolder.getInstance().getTemplate(18556));
			sp.setLoc(Location.findPointToStay(actor, 100, 120));
			sp.setReflection(actor.getReflection());
			NpcInstance npc = sp.doSpawn(true);
			npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 100);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		_hpCount = 0;
		super.onEvtDead(killer);
	}
}