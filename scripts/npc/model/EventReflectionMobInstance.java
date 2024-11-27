package npc.model;

import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.model.Creature;
import l2.gameserver.model.instances.DoorInstance;
import l2.gameserver.model.instances.MonsterInstance;
import l2.gameserver.templates.npc.NpcTemplate;

public class EventReflectionMobInstance extends MonsterInstance
{
	public EventReflectionMobInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	protected void onDeath(Creature killer)
	{
		super.onDeath(killer);
		if(getReflection() == killer.getReflection() && getReflection() != ReflectionManager.DEFAULT)
		{
			switch(getNpcId())
			{
				case 20001:
				{
					DoorInstance door = getReflection().getDoor(25150002);
					if(door == null)
						break;
					door.openMe();
					break;
				}
				case 20002:
				{
					DoorInstance door = getReflection().getDoor(25150003);
					if(door == null)
						break;
					door.openMe();
					break;
				}
				case 20003:
				{
					DoorInstance door = getReflection().getDoor(25150004);
					if(door == null)
						break;
					door.openMe();
				}
			}
		}
	}
}