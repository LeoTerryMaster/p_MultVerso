package ai.custom;

import l2.gameserver.ai.Fighter;
import l2.gameserver.model.Creature;
import l2.gameserver.model.entity.Reflection;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.instances.ReflectionBossInstance;
import l2.gameserver.stats.Stats;
import l2.gameserver.stats.funcs.FuncSet;

public class LabyrinthLostBeholder extends Fighter
{
	public LabyrinthLostBeholder(NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();
		Reflection r = actor.getReflection();
		if(!r.isDefault() && checkMates(actor.getNpcId()) && findLostCaptain() != null)
		{
			findLostCaptain().addStatFunc(new FuncSet(Stats.MAGIC_DEFENCE, 48, this, (double) findLostCaptain().getTemplate().baseMDef * 0.66));
		}
		super.onEvtDead(killer);
	}
	
	private boolean checkMates(int id)
	{
		for(NpcInstance n : getActor().getReflection().getNpcs())
		{
			if(n.getNpcId() != id || n.isDead())
				continue;
			return false;
		}
		return true;
	}
	
	private NpcInstance findLostCaptain()
	{
		for(NpcInstance n : getActor().getReflection().getNpcs())
		{
			if(!(n instanceof ReflectionBossInstance))
				continue;
			return n;
		}
		return null;
	}
}