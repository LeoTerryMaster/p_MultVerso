package npc.model.residences.clanhall;

import l2.gameserver.model.entity.residence.Residence;
import l2.gameserver.templates.npc.NpcTemplate;

public class DoormanInstance extends npc.model.residences.DoormanInstance
{
	public DoormanInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public int getOpenPriv()
	{
		return 1024;
	}
	
	@Override
	public Residence getResidence()
	{
		return getClanHall();
	}
}