package npc.model;

import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.templates.npc.NpcTemplate;

public final class PassagewayMobWithHerbInstance extends MonsterInstance
{
	public static final int FieryDemonBloodHerb = 9849;
	
	public PassagewayMobWithHerbInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void calculateRewards(Creature lastAttacker)
	{
		if(lastAttacker == null)
		{
			return;
		}
		super.calculateRewards(lastAttacker);
		if(lastAttacker.isPlayable())
		{
			dropItem(lastAttacker.getPlayer(), 9849, 1);
		}
	}
}