package npc.model.residences.castle;

import l2.gameserver.model.Creature;
import l2.gameserver.model.Spawner;
import l2.gameserver.model.instances.residences.SiegeToggleNpcInstance;
import l2.gameserver.templates.npc.NpcTemplate;

import java.util.HashSet;
import java.util.Set;

public class CastleControlTowerInstance extends SiegeToggleNpcInstance
{
	private final Set<Spawner> _spawnList = new HashSet<>();
	
	public CastleControlTowerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onDeathImpl(Creature killer)
	{
		for(Spawner spawn : _spawnList)
		{
			spawn.stopRespawn();
		}
		_spawnList.clear();
	}
	
	@Override
	public void register(Spawner spawn)
	{
		_spawnList.add(spawn);
	}
}