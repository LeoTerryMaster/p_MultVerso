package l2.gameserver.model.instances;

import l2.gameserver.model.Creature;
import l2.gameserver.templates.npc.NpcTemplate;

public class ReflectionBossInstance extends RaidBossInstance
{
	private static final int COLLAPSE_AFTER_DEATH_TIME = 5;
	
	public ReflectionBossInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	protected void onDeath(Creature killer)
	{
		getMinionList().unspawnMinions();
		super.onDeath(killer);
		clearReflection();
	}
	
	protected void clearReflection()
	{
		getReflection().clearReflection(5, true);
	}
}