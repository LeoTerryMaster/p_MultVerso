package l2.gameserver.handler.admincommands.impl;

import l2.gameserver.data.xml.holder.BuyListHolder;
import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.BuyList;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.utils.GameStats;

public class AdminShop implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().UseGMShop)
		{
			return false;
		}
		switch(command)
		{
			case admin_buy:
			{
				try
				{
					handleBuyRequest(activeChar, fullString.substring(10));
				}
				catch(IndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Please specify buylist.");
				}
				break;
			}
			case admin_gmshop:
			{
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/gmshops.htm"));
				break;
			}
			case admin_tax:
			{
				activeChar.sendMessage("TaxSum: " + GameStats.getTaxSum());
				break;
			}
			case admin_taxclear:
			{
				GameStats.addTax(-GameStats.getTaxSum());
				activeChar.sendMessage("TaxSum: " + GameStats.getTaxSum());
			}
		}
		return true;
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	private void handleBuyRequest(Player activeChar, String command)
	{
		int val = -1;
		try
		{
			val = Integer.parseInt(command);
		}
		catch(Exception e)
		{
			
		}
		BuyListHolder.NpcTradeList list = BuyListHolder.getInstance().getBuyList(val);
		if(list != null)
		{
			activeChar.sendPacket(new BuyList(list, activeChar, 0.0));
		}
		activeChar.sendActionFailed();
	}
	
	private enum Commands
	{
		admin_buy,
		admin_gmshop,
		admin_tax,
		admin_taxclear;
	}
}