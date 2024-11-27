package l2.gameserver.model.instances;

import l2.gameserver.model.Player;
import l2.gameserver.model.entity.SevenSigns;
import l2.gameserver.model.entity.residence.Residence;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.tables.PetDataTable;
import l2.gameserver.templates.npc.NpcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.StringTokenizer;

public final class WyvernManagerInstance extends NpcInstance
{
	private static final Logger _log = LoggerFactory.getLogger(WyvernManagerInstance.class);
	
	public WyvernManagerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
		{
			return;
		}
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken();
		boolean condition = validateCondition(player);
		NpcHtmlMessage html;
		if(actualCommand.equalsIgnoreCase("RideHelp"))
		{
			html = new NpcHtmlMessage(player, this);
			html.setFile("wyvern/help_ride.htm");
			html.replace("%npcname%", "Wyvern Manager " + getName());
			player.sendPacket(html);
			player.sendActionFailed();
		}
		if(condition)
		{
			if(actualCommand.equalsIgnoreCase("RideWyvern") && player.isClanLeader())
			{
				if(!player.isRiding() || !PetDataTable.isStrider(player.getMountNpcId()))
				{
					html = new NpcHtmlMessage(player, this);
					html.setFile("wyvern/not_ready.htm");
					html.replace("%npcname%", "Wyvern Manager " + getName());
					player.sendPacket(html);
				}
				else if(player.getInventory().getItemByItemId(1460) == null || player.getInventory().getItemByItemId(1460).getCount() < 10)
				{
					html = new NpcHtmlMessage(player, this);
					html.setFile("wyvern/havenot_cry.htm");
					html.replace("%npcname%", "Wyvern Manager " + getName());
					player.sendPacket(html);
				}
				else if(SevenSigns.getInstance().getCurrentPeriod() == 3 && SevenSigns.getInstance().getCabalHighestScore() == 3)
				{
					html = new NpcHtmlMessage(player, this);
					html.setFile("wyvern/no_ride_dusk.htm");
					html.replace("%npcname%", "Wyvern Manager " + getName());
					player.sendPacket(html);
				}
				else if(player.getInventory().destroyItemByItemId(1460, 10))
				{
					player.setMount(12621, player.getMountObjId(), player.getMountLevel());
					html = new NpcHtmlMessage(player, this);
					html.setFile("wyvern/after_ride.htm");
					html.replace("%npcname%", "Wyvern Manager " + getName());
					player.sendPacket(html);
				}
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		if(!validateCondition(player))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(player, this);
			html.setFile("wyvern/lord_only.htm");
			html.replace("%npcname%", "Wyvern Manager " + getName());
			player.sendPacket(html);
			player.sendActionFailed();
			return;
		}
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile("wyvern/lord_here.htm");
		html.replace("%Char_name%", String.valueOf(player.getName()));
		html.replace("%npcname%", "Wyvern Manager " + getName());
		player.sendPacket(html);
		player.sendActionFailed();
	}
	
	private boolean validateCondition(Player player)
	{
		Residence residence = getCastle();
		if(residence != null && residence.getId() > 0 && player.getClan() != null && residence.getOwnerId() == player.getClanId() && player.isClanLeader())
		{
			return true;
		}
		residence = getClanHall();
		return residence != null && residence.getId() > 0 && player.getClan() != null && residence.getOwnerId() == player.getClanId() && player.isClanLeader();
	}
}