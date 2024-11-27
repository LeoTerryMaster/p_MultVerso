package npc.model.residences.castle;

import l2.gameserver.model.Player;
import l2.gameserver.model.entity.residence.Castle;
import l2.gameserver.model.entity.residence.Residence;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.ReflectionUtils;

public class DoormanInstance extends npc.model.residences.DoormanInstance
{
	private final Location[] _locs = new Location[2];
	
	public DoormanInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		for(int i = 0;i < _locs.length;++i)
		{
			String loc = template.getAIParams().getString("tele_loc" + i, null);
			if(loc == null)
				continue;
			_locs[i] = Location.parseLoc(loc);
		}
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
		{
			return;
		}
		int cond = getCond(player);
		switch(cond)
		{
			case 0:
			{
				if(command.equalsIgnoreCase("openDoors"))
				{
					for(int i : _doors)
					{
						ReflectionUtils.getDoor(i).openMe(player, true);
					}
					break;
				}
				if(command.equalsIgnoreCase("closeDoors"))
				{
					for(int i : _doors)
					{
						ReflectionUtils.getDoor(i).closeMe(player, true);
					}
					break;
				}
				if(!command.startsWith("tele"))
					break;
				int id = Integer.parseInt(command.substring(4, 5));
				Location loc = _locs[id];
				if(loc == null)
					break;
				player.teleToLocation(loc);
				break;
			}
			case 1:
			{
				if(command.startsWith("tele"))
				{
					int id = Integer.parseInt(command.substring(4, 5));
					Location loc = _locs[id];
					if(loc == null)
						break;
					player.teleToLocation(loc);
					break;
				}
				player.sendPacket(new NpcHtmlMessage(player, this, _siegeDialog, 0));
				break;
			}
			case 2:
			{
				player.sendPacket(new NpcHtmlMessage(player, this, _failDialog, 0));
			}
		}
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		String filename = null;
		int cond = getCond(player);
		switch(cond)
		{
			case 0:
			case 1:
			{
				filename = _mainDialog;
				break;
			}
			case 2:
			{
				filename = _failDialog;
			}
		}
		player.sendPacket(new NpcHtmlMessage(player, this, filename, val));
	}
	
	@Override
	protected int getCond(Player player)
	{
		Castle residence = getCastle();
		Clan residenceOwner = residence.getOwner();
		if(residenceOwner != null && player.getClan() == residenceOwner && (player.getClanPrivileges() & getOpenPriv()) == getOpenPriv())
		{
			if(residence.getSiegeEvent().isInProgress())
			{
				return 1;
			}
			return 0;
		}
		return 2;
	}
	
	@Override
	public int getOpenPriv()
	{
		return 32768;
	}
	
	@Override
	public Residence getResidence()
	{
		return getCastle();
	}
}