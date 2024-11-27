package l2.gameserver.model.instances;

import l2.commons.lang.reference.HardReference;
import l2.gameserver.ai.CtrlIntention;
import l2.gameserver.model.Creature;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.reference.L2Reference;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;
import l2.gameserver.network.l2.s2c.MyTargetSelected;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.ShowTownMap;
import l2.gameserver.network.l2.s2c.StaticObject;
import l2.gameserver.scripts.Events;
import l2.gameserver.templates.StaticObjectTemplate;
import l2.gameserver.utils.Location;

import java.util.Collections;
import java.util.List;

public class StaticObjectInstance extends GameObject
{
	private final HardReference<StaticObjectInstance> reference;
	private final StaticObjectTemplate _template;
	private int _meshIndex;
	
	public StaticObjectInstance(int objectId, StaticObjectTemplate template)
	{
		super(objectId);
		_template = template;
		reference = new L2Reference<>(this);
	}
	
	@Override
	public HardReference<StaticObjectInstance> getRef()
	{
		return reference;
	}
	
	public int getUId()
	{
		return _template.getUId();
	}
	
	public int getType()
	{
		return _template.getType();
	}
	
	@Override
	public void onAction(Player player, boolean shift)
	{
		if(Events.onAction(player, this, shift))
		{
			return;
		}
		if(player.getTarget() != this)
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			return;
		}
		MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
		player.sendPacket(my);
		if(!isInRange(player, (long) getActingRange()))
		{
			if(!player.getAI().isIntendingInteract(this))
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			return;
		}
		if(_template.getType() == 0)
		{
			player.sendPacket(new NpcHtmlMessage(player, getUId(), "newspaper/arena.htm", 0));
		}
		else if(_template.getType() == 2)
		{
			player.sendPacket(new ShowTownMap(_template.getFilePath(), _template.getMapX(), _template.getMapY()));
			player.sendActionFailed();
		}
	}
	
	@Override
	public int getActingRange()
	{
		switch(_template.getType())
		{
			case 1:
			{
				return 150;
			}
		}
		return 300;
	}
	
	@Override
	public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
	{
		return Collections.singletonList(new StaticObject(this));
	}
	
	@Override
	public boolean isAttackable(Creature attacker)
	{
		return false;
	}
	
	public void broadcastInfo(boolean force)
	{
		StaticObject p = new StaticObject(this);
		for(Player player : World.getAroundPlayers(this))
		{
			player.sendPacket(p);
		}
	}
	
	@Override
	public int getGeoZ(Location loc)
	{
		return loc.z;
	}
	
	public int getMeshIndex()
	{
		return _meshIndex;
	}
	
	public void setMeshIndex(int meshIndex)
	{
		_meshIndex = meshIndex;
	}
}