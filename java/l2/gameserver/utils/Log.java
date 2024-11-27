package l2.gameserver.utils;

import l2.gameserver.Config;
import l2.gameserver.data.xml.holder.ItemHolder;
import l2.gameserver.model.GameObject;
import l2.gameserver.model.Player;
import l2.gameserver.model.items.ItemInstance;
import l2.gameserver.templates.item.ItemTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log
{
	private static final Logger _log = LoggerFactory.getLogger(Log.class);
	private static final Logger _logChat = LoggerFactory.getLogger((String) "chat");
	private static final Logger _logGm = LoggerFactory.getLogger((String) "gmactions");
	private static final Logger _logItems = LoggerFactory.getLogger((String) "item");
	private static final Logger _logGame = LoggerFactory.getLogger((String) "game");
	private static final Logger _logDebug = LoggerFactory.getLogger((String) "debug");
	
	public static void add(String text, String cat, Player player)
	{
		StringBuilder output = new StringBuilder();
		output.append(cat);
		if(player != null)
		{
			output.append(' ');
			output.append(player);
		}
		output.append(' ');
		output.append(text);
		_logGame.info(output.toString());
	}
	
	public static void add(String text, String cat)
	{
		add(text, cat, null);
	}
	
	public static void debug(String text)
	{
		_logDebug.debug(text);
	}
	
	public static void debug(String text, Throwable t)
	{
		_logDebug.debug(text, t);
	}
	
	public static void LogChat(String type, String player, String target, String text, int identifier)
	{
		if(!Config.LOG_CHAT)
		{
			return;
		}
		StringBuilder output = new StringBuilder();
		output.append(type);
		if(identifier > 0)
		{
			output.append(' ');
			output.append(identifier);
		}
		output.append(' ');
		output.append('[');
		output.append(player);
		if(target != null)
		{
			output.append(" -> ");
			output.append(target);
		}
		output.append(']');
		output.append(' ');
		output.append(text);
		_logChat.info(output.toString());
	}
	
	public static void LogCommand(Player player, GameObject target, String command, boolean success)
	{
		StringBuilder output = new StringBuilder();
		if(success)
		{
			output.append("SUCCESS");
		}
		else
		{
			output.append("FAIL   ");
		}
		output.append(' ');
		output.append(player);
		if(target != null)
		{
			output.append(" -> ");
			output.append(target);
		}
		output.append(' ');
		output.append(command);
		_logGm.info(output.toString());
	}
	
	public static void LogItem(Player activeChar, ItemLog logType, ItemInstance item)
	{
		LogItem(activeChar, logType, item, item.getItemId(), item.getCount(), 0, 0);
	}
	
	public static void LogItem(Player activeChar, ItemLog logType, ItemInstance item, long count)
	{
		LogItem(activeChar, logType, item, item.getItemId(), count, 0, 0);
	}
	
	public static void LogItem(Player activeChar, ItemLog logType, ItemInstance item, long count, long price)
	{
		LogItem(activeChar, logType, item, item.getItemId(), count, price, 0);
	}
	
	public static void LogItem(Player activeChar, ItemLog logType, ItemInstance item, long count, long price, int paramId)
	{
		LogItem(activeChar, logType, item, item.getItemId(), count, price, paramId);
	}
	
	public static void LogItem(Player activeChar, ItemLog logType, int itemId, long count)
	{
		LogItem(activeChar, logType, null, itemId, count, 0, 0);
	}
	
	public static void LogItem(Player activeChar, ItemLog logType, int itemId, long count, long price)
	{
		LogItem(activeChar, logType, null, itemId, count, price, 0);
	}
	
	public static void LogItem(Player activeChar, ItemLog logType, int itemId, long count, long price, int paramId)
	{
		LogItem(activeChar, logType, null, itemId, count, price, paramId);
	}
	
	private static void LogItem(Player activeChar, ItemLog logType, ItemInstance item, int itemId, long count, long price, int paramId)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(logType);
		sb.append(' ');
		sb.append(activeChar.getName());
		sb.append('[').append(activeChar.getObjectId()).append(']').append(' ');
		sb.append('(').append("IP: ").append(activeChar.getIP()).append(' ').append("Account: ").append(activeChar.getAccountName()).append(')').append(' ');
		sb.append('(').append("X: ").append(activeChar.getX()).append(' ').append("Y: ").append(activeChar.getY()).append(' ').append("Z: ").append(activeChar.getZ()).append(')');
		sb.append(' ');
		sb.append(itemId);
		sb.append(' ');
		if(item != null)
		{
			if(item.getEnchantLevel() > 0)
			{
				sb.append('+');
				sb.append(item.getEnchantLevel());
				sb.append(' ');
			}
			sb.append(item.getTemplate().getName());
			if(!item.getTemplate().getAdditionalName().isEmpty())
			{
				sb.append(' ');
				sb.append('<').append(item.getTemplate().getAdditionalName()).append('>');
			}
			sb.append(' ');
			if(item.getAttributes().getValue() > 0)
			{
				sb.append('(');
				sb.append("Fire: ");
				sb.append(item.getAttributes().getFire());
				sb.append(' ');
				sb.append("Water: ");
				sb.append(item.getAttributes().getWater());
				sb.append(' ');
				sb.append("Wind: ");
				sb.append(item.getAttributes().getWind());
				sb.append(' ');
				sb.append("Earth: ");
				sb.append(item.getAttributes().getEarth());
				sb.append(' ');
				sb.append("Holy: ");
				sb.append(item.getAttributes().getHoly());
				sb.append(' ');
				sb.append("Unholy: ");
				sb.append(item.getAttributes().getUnholy());
				sb.append(')');
				sb.append(' ');
			}
			sb.append('(');
			sb.append(item.getCount());
			sb.append(')');
			sb.append('[');
			sb.append(item.getObjectId());
			sb.append(']');
		}
		else
		{
			ItemTemplate it = ItemHolder.getInstance().getTemplate(itemId);
			sb.append(it.getName());
			if(!it.getAdditionalName().isEmpty())
			{
				sb.append(' ');
				sb.append('<').append(it.getAdditionalName()).append('>');
			}
		}
		sb.append(' ');
		sb.append("Count: ").append(count);
		switch(logType)
		{
			case CraftCreate:
			case CraftDelete:
			{
				sb.append(' ');
				sb.append("Recipe: ").append(paramId);
				break;
			}
			case PrivateStoreBuy:
			case PrivateStoreSell:
			case RecipeShopBuy:
			case RecipeShopSell:
			{
				sb.append(' ');
				sb.append("Price: ").append(price);
				break;
			}
			case MultiSellIngredient:
			case MultiSellProduct:
			{
				sb.append(' ');
				sb.append("MultiSell: ").append(paramId);
				break;
			}
			case NpcBuy:
			{
				sb.append(' ');
				sb.append("BuyList: ").append(paramId);
				sb.append(' ');
				sb.append("Price: ").append(price);
				break;
			}
			case NpcCreate:
			case NpcDelete:
			{
				sb.append(' ');
				sb.append("NPC: ").append(paramId);
				break;
			}
			case QuestCreate:
			case QuestDelete:
			{
				sb.append(' ');
				sb.append("Quest: ").append(paramId);
				break;
			}
			case EventCreate:
			case EventDelete:
			{
				sb.append(' ');
				sb.append("Event: ").append(paramId);
			}
		}
		_logItems.info(sb.toString());
	}
	
	public static void LogPetition(Player fromChar, Integer Petition_type, String Petition_text)
	{
	}
	
	public static void LogAudit(Player player, String type, String msg)
	{
	}
	
	public enum ItemLog
	{
		Create,
		Delete,
		Drop,
		PvPDrop,
		Crystalize,
		EnchantFail,
		EnchantSuccess,
		Pickup,
		PetPickup,
		PartyPickup,
		PrivateStoreBuy,
		PrivateStoreSell,
		RecipeShopBuy,
		RecipeShopSell,
		CraftCreate,
		CraftDelete,
		TradeBuy,
		TradeSell,
		FromPet,
		ToPet,
		PostRecieve,
		PostSend,
		PostCancel,
		PostExpire,
		PostPrice,
		RefundSell,
		RefundReturn,
		WarehouseDeposit,
		WarehouseWithdraw,
		FreightWithdraw,
		FreightDeposit,
		ClanWarehouseDeposit,
		ClanWarehouseWithdraw,
		ExtractCreate,
		ExtractDelete,
		NpcBuy,
		NpcCreate,
		NpcDelete,
		MultiSellIngredient,
		MultiSellProduct,
		QuestCreate,
		QuestDelete,
		EventCreate,
		EventDelete;
	}
}