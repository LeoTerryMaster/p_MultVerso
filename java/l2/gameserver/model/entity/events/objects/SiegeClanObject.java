package l2.gameserver.model.entity.events.objects;

import l2.gameserver.model.Player;
import l2.gameserver.model.entity.events.impl.SiegeEvent;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.components.IStaticPacket;
import l2.gameserver.network.l2.s2c.L2GameServerPacket;

import java.io.Serializable;
import java.util.Comparator;

public class SiegeClanObject implements Serializable
{
	private final Clan _clan;
	private final long _date;
	private String _type;
	private NpcInstance _flag;
	
	public SiegeClanObject(String type, Clan clan, long param)
	{
		this(type, clan, 0, System.currentTimeMillis());
	}
	
	public SiegeClanObject(String type, Clan clan, long param, long date)
	{
		_type = type;
		_clan = clan;
		_date = date;
	}
	
	public int getObjectId()
	{
		return _clan.getClanId();
	}
	
	public Clan getClan()
	{
		return _clan;
	}
	
	public NpcInstance getFlag()
	{
		return _flag;
	}
	
	public void setFlag(NpcInstance npc)
	{
		_flag = npc;
	}
	
	public void deleteFlag()
	{
		if(_flag != null)
		{
			_flag.deleteMe();
			_flag = null;
		}
	}
	
	public String getType()
	{
		return _type;
	}
	
	public void setType(String type)
	{
		_type = type;
	}
	
	public void broadcast(IStaticPacket... packet)
	{
		getClan().broadcastToOnlineMembers(packet);
	}
	
	public void broadcast(L2GameServerPacket... packet)
	{
		getClan().broadcastToOnlineMembers(packet);
	}
	
	public void setEvent(boolean start, SiegeEvent event)
	{
		if(start)
		{
			for(Player player : _clan.getOnlineMembers(0))
			{
				player.addEvent(event);
				player.broadcastCharInfo();
			}
		}
		else
		{
			for(Player player : _clan.getOnlineMembers(0))
			{
				player.removeEvent(event);
				player.broadcastCharInfo();
			}
		}
	}
	
	public boolean isParticle(Player player)
	{
		return true;
	}
	
	public long getParam()
	{
		return 0;
	}
	
	public long getDate()
	{
		return _date;
	}
	
	public static class SiegeClanComparatorImpl implements Comparator<SiegeClanObject>
	{
		private static final SiegeClanComparatorImpl _instance = new SiegeClanComparatorImpl();
		
		public static SiegeClanComparatorImpl getInstance()
		{
			return _instance;
		}
		
		@Override
		public int compare(SiegeClanObject o1, SiegeClanObject o2)
		{
			return o2.getParam() < o1.getParam() ? -1 : o2.getParam() == o1.getParam() ? 0 : 1;
		}
	}
}