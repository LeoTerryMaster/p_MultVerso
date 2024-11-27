package l2.gameserver.model.entity.events.objects;

import l2.gameserver.dao.SiegePlayerDAO;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.entity.events.impl.SiegeEvent;
import l2.gameserver.model.entity.residence.Residence;
import l2.gameserver.model.pledge.Clan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CTBSiegeClanObject extends SiegeClanObject
{
	private final List<Integer> _players = new ArrayList<>();
	private long _npcId;
	
	public CTBSiegeClanObject(String type, Clan clan, long param, long date)
	{
		super(type, clan, param, date);
		_npcId = param;
	}
	
	public CTBSiegeClanObject(String type, Clan clan, long param)
	{
		this(type, clan, param, System.currentTimeMillis());
	}
	
	public void select(Residence r)
	{
		_players.addAll(SiegePlayerDAO.getInstance().select(r, getObjectId()));
	}
	
	public List<Integer> getPlayers()
	{
		return _players;
	}
	
	@Override
	public void setEvent(boolean start, SiegeEvent event)
	{
		Iterator<Integer> iterator = getPlayers().iterator();
		while(iterator.hasNext())
		{
			int i = iterator.next();
			Player player = GameObjectsStorage.getPlayer(i);
			if(player == null)
				continue;
			if(start)
			{
				player.addEvent(event);
			}
			else
			{
				player.removeEvent(event);
			}
			player.broadcastCharInfo();
		}
	}
	
	@Override
	public boolean isParticle(Player player)
	{
		return _players.contains(player.getObjectId());
	}
	
	@Override
	public long getParam()
	{
		return _npcId;
	}
	
	public void setParam(int npcId)
	{
		_npcId = npcId;
	}
}