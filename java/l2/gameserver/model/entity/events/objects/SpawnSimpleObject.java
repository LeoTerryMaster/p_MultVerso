package l2.gameserver.model.entity.events.objects;

import l2.gameserver.model.entity.events.GlobalEvent;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.NpcUtils;

public class SpawnSimpleObject implements SpawnableObject
{
	private final int _npcId;
	private final Location _loc;
	private NpcInstance _npc;
	
	public SpawnSimpleObject(int npcId, Location loc)
	{
		_npcId = npcId;
		_loc = loc;
	}
	
	@Override
	public void spawnObject(GlobalEvent event)
	{
		_npc = NpcUtils.spawnSingle(_npcId, _loc, event.getReflection());
		_npc.addEvent(event);
	}
	
	@Override
	public void despawnObject(GlobalEvent event)
	{
		_npc.removeEvent(event);
		_npc.deleteMe();
	}
	
	@Override
	public void refreshObject(GlobalEvent event)
	{
	}
}