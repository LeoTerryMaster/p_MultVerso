package npc.model.residences.castle;

import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.templates.npc.NpcTemplate;
import l2.gameserver.utils.Log;
import l2.gameserver.utils.WarehouseFunctions;

public class WarehouseInstance extends NpcInstance
{
	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;
	
	public WarehouseInstance(int objectId, NpcTemplate template)
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
		if((player.getClanPrivileges() & 262144) != 262144)
		{
			player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		if(player.getEnchantScroll() != null)
		{
			Log.add("Player " + player.getName() + " trying to use enchant exploit[CastleWarehouse], ban this player!", "illegal-actions");
			player.kick();
			return;
		}
		if(command.startsWith("WithdrawP"))
		{
			int val = Integer.parseInt(command.substring(10));
			if(val == 99)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(player, this);
				html.setFile("warehouse/personal.htm");
				player.sendPacket(html);
			}
			else
			{
				WarehouseFunctions.showRetrieveWindow(player, val);
			}
		}
		else if(command.equals("DepositP"))
		{
			WarehouseFunctions.showDepositWindow(player);
		}
		else if(command.startsWith("WithdrawC"))
		{
			int val = Integer.parseInt(command.substring(10));
			if(val == 99)
			{
				NpcHtmlMessage html2 = new NpcHtmlMessage(player, this);
				html2.setFile("warehouse/clan.htm");
				player.sendPacket(html2);
			}
			else
			{
				WarehouseFunctions.showWithdrawWindowClan(player, val);
			}
		}
		else if(command.equals("DepositC"))
		{
			WarehouseFunctions.showDepositWindowClan(player);
		}
		else if(command.startsWith("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch(IndexOutOfBoundsException e)
			{
			}
			catch(NumberFormatException e)
			{
				
			}
			showChatWindow(player, val);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		player.sendActionFailed();
		String filename = "castle/warehouse/castlewarehouse-no.htm";
		int condition = validateCondition(player);
		if(condition > 0)
		{
			if(condition == 1)
			{
				filename = "castle/warehouse/castlewarehouse-busy.htm";
			}
			else if(condition == 2)
			{
				filename = val == 0 ? "castle/warehouse/castlewarehouse.htm" : "castle/warehouse/castlewarehouse-" + val + ".htm";
			}
		}
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile(filename);
		player.sendPacket(html);
	}
	
	protected int validateCondition(Player player)
	{
		if(player.isGM())
		{
			return 2;
		}
		if(getCastle() != null && getCastle().getId() > 0 && player.getClan() != null)
		{
			if(getCastle().getSiegeEvent().isInProgress())
			{
				return 1;
			}
			if(getCastle().getOwnerId() == player.getClanId())
			{
				return 2;
			}
		}
		return 0;
	}
}