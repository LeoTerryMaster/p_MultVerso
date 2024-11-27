package npc.model;

import l2.gameserver.instancemanager.SpawnManager;
import l2.gameserver.model.instances.DoorInstance;
import l2.gameserver.model.instances.RaidBossInstance;
import l2.gameserver.templates.npc.NpcTemplate;

public class AndreasVanHalterInstance extends RaidBossInstance
{
	private static final String SPAWN_GROUP_NAME = "[guard_of_andreas]";
	private static final int[] DOORS_IDS = {19160016, 19160017};
	
	public AndreasVanHalterInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		SpawnManager.getInstance().spawn("[guard_of_andreas]");
	}
	
	@Override
	protected void onDespawn()
	{
		super.onDespawn();
		SpawnManager.getInstance().despawn("[guard_of_andreas]");
		int[] arrn = DOORS_IDS;
		int n = arrn.length;
		for(int i = 0;i < n;++i)
		{
			Integer doorId = arrn[i];
			DoorInstance doorInstance = getReflection().getDoor(doorId.intValue());
			if(doorInstance == null)
				continue;
			doorInstance.openMe();
		}
	}
}