package l2.gameserver.handler.admincommands.impl;

import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.Element;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.network.l2.s2c.InventoryUpdate;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.SystemMessage2;
import l2.gameserver.utils.ItemFunctions;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.Log;

public class AdminCreateItem implements IAdminCommandHandler
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
			case admin_itemcreate:
			{
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/itemcreation.htm"));
				break;
			}
			case admin_ci:
			case admin_create_item:
			{
				try
				{
					if(wordList.length < 2)
					{
						activeChar.sendMessage("USAGE: create_item id [count]");
						return false;
					}
					int item_id = Integer.parseInt(wordList[1]);
					long item_count = wordList.length < 3 ? 1 : Long.parseLong(wordList[2]);
					createItem(activeChar, item_id, item_count);
				}
				catch(NumberFormatException e)
				{
					activeChar.sendMessage("USAGE: create_item id [count]");
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/itemcreation.htm"));
				break;
			}
			case admin_spreaditem:
			{
				try
				{
					int id = Integer.parseInt(wordList[1]);
					int num = wordList.length > 2 ? Integer.parseInt(wordList[2]) : 1;
					long count = wordList.length > 3 ? Long.parseLong(wordList[3]) : 1;
					for(int i = 0;i < num;++i)
					{
						ItemInstance createditem = ItemFunctions.createItem(id);
						createditem.setCount(count);
						createditem.dropMe(activeChar, Location.findPointToStay(activeChar, 100));
					}
					break;
				}
				catch(NumberFormatException e)
				{
					activeChar.sendMessage("Specify a valid number.");
					break;
				}
				catch(StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Can't create this item.");
					break;
				}
			}
			case admin_create_item_element:
			{
				try
				{
					if(wordList.length < 4)
					{
						activeChar.sendMessage("USAGE: create_item_attribue [id] [element id] [value]");
						return false;
					}
					int item_id = Integer.parseInt(wordList[1]);
					int elementId = Integer.parseInt(wordList[2]);
					int value = Integer.parseInt(wordList[3]);
					if(elementId > 5 || elementId < 0)
					{
						activeChar.sendMessage("Improper element Id");
						return false;
					}
					if(value < 1 || value > 300)
					{
						activeChar.sendMessage("Improper element value");
						return false;
					}
					ItemInstance item = createItem(activeChar, item_id, 1);
					Element element = Element.getElementById(elementId);
					item.setAttributeElement(element, item.getAttributeElementValue(element, false) + value);
					activeChar.sendPacket(new InventoryUpdate().addModifiedItem(item));
				}
				catch(NumberFormatException e)
				{
					activeChar.sendMessage("USAGE: create_item id [count]");
				}
				activeChar.sendPacket(new NpcHtmlMessage(5).setFile("data/html/admin/itemcreation.htm"));
			}
		}
		return true;
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	private ItemInstance createItem(Player activeChar, int itemId, long count)
	{
		ItemInstance createditem = ItemFunctions.createItem(itemId);
		createditem.setCount(count);
		Log.LogItem(activeChar, Log.ItemLog.Create, createditem);
		activeChar.getInventory().addItem(createditem);
		if(!createditem.isStackable())
		{
			for(long i = 0;i < count - 1;++i)
			{
				createditem = ItemFunctions.createItem(itemId);
				Log.LogItem(activeChar, Log.ItemLog.Create, createditem);
				activeChar.getInventory().addItem(createditem);
			}
		}
		activeChar.sendPacket(SystemMessage2.obtainItems(itemId, count, 0));
		return createditem;
	}
	
	private enum Commands
	{
		admin_itemcreate,
		admin_create_item,
		admin_ci,
		admin_spreaditem,
		admin_create_item_element;
	}
}