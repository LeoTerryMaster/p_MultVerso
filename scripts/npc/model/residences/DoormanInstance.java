package npc.model.residences;

import l2.gameserver.model.Player;
import l2.gameserver.model.entity.residence.Residence;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.ReflectionUtils;

public abstract class DoormanInstance extends NpcInstance
{
	protected static final int COND_OWNER = 0;
	protected static final int COND_SIEGE = 1;
	protected static final int COND_FAIL = 2;
	protected String _siegeDialog;
	protected String _mainDialog;
	protected String _failDialog;
	protected int[] _doors;
	
	public DoormanInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setDialogs();
		_doors = template.getAIParams().getIntegerArray("doors");
	}
	
	public void setDialogs()
	{
		_siegeDialog = getTemplate().getAIParams().getString("siege_dialog");
		_mainDialog = getTemplate().getAIParams().getString("main_dialog");
		_failDialog = getTemplate().getAIParams().getString("fail_dialog");
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
						ReflectionUtils.getDoor(i).openMe();
					}
				}
				else
				{
					if(!command.equalsIgnoreCase("closeDoors"))
						break;
					for(int i : _doors)
					{
						ReflectionUtils.getDoor(i).closeMe();
					}
				}
				break;
			}
			case 1:
			{
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
			{
				filename = _mainDialog;
				break;
			}
			case 1:
			{
				filename = _siegeDialog;
				break;
			}
			case 2:
			{
				filename = _failDialog;
			}
		}
		player.sendPacket(new NpcHtmlMessage(player, this, filename, val));
	}
	
	protected int getCond(Player player)
	{
		Residence residence = getResidence();
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
	
	public abstract int getOpenPriv();
	
	public abstract Residence getResidence();
}