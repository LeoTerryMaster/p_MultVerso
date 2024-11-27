package l2.gameserver.utils;

import l2.gameserver.Announcements;
import l2.gameserver.Config;
import l2.gameserver.dao.CharacterDAO;
import l2.gameserver.instancemanager.CursedWeaponsManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.network.l2.components.ChatType;
import l2.gameserver.network.l2.components.CustomMessage;

public final class AdminFunctions
{
	public static final Location JAIL_SPAWN = new Location(-114648, -249384, -2984);
	
	private AdminFunctions()
	{
	}
	
	public static boolean kick(String player, String reason)
	{
		Player plyr = World.getPlayer(player);
		if(plyr == null)
		{
			return false;
		}
		return kick(plyr, reason);
	}
	
	public static boolean kick(Player player, String reason)
	{
		if(Config.ALLOW_CURSED_WEAPONS && Config.DROP_CURSED_WEAPONS_ON_KICK && player.isCursedWeaponEquipped())
		{
			player.setPvpFlag(0);
			CursedWeaponsManager.getInstance().dropPlayer(player);
		}
		player.kick();
		return true;
	}
	
	public static String banChat(Player adminChar, String adminName, String charName, int val, String reason)
	{
		Player player = World.getPlayer(charName);
		if(player != null)
		{
			charName = player.getName();
		}
		else if(CharacterDAO.getInstance().getObjectIdByName(charName) == 0)
		{
			return "Player " + charName + " not found.";
		}
		if((adminName == null || adminName.isEmpty()) && adminChar != null)
		{
			adminName = adminChar.getName();
		}
		if(reason == null || reason.isEmpty())
		{
			reason = "no arguments";
		}
		String announce = null;
		String result;
		if(val == 0)
		{
			if(adminChar != null && !adminChar.getPlayerAccess().CanUnBanChat)
			{
				return "You do not have permission to remove the chat ban.";
			}
			if(Config.BANCHAT_ANNOUNCE)
			{
				announce = Config.BANCHAT_ANNOUNCE_NICK && adminName != null && !adminName.isEmpty() ? adminName + " chat ban removed from player " + charName + "." : "From player " + charName + " unban chat.";
			}
			Log.add(adminName + " chat ban removed from player " + charName + ".", "banchat", adminChar);
			result = "You remove a chat ban from player " + charName + ".";
		}
		else if(val < 0)
		{
			if(adminChar != null && adminChar.getPlayerAccess().BanChatMaxValue > 0)
			{
				return "You can ban no more than " + adminChar.getPlayerAccess().BanChatMaxValue + " minutes.";
			}
			if(Config.BANCHAT_ANNOUNCE)
			{
				announce = Config.BANCHAT_ANNOUNCE_NICK && adminName != null && !adminName.isEmpty() ? adminName + " banned the chat player " + charName + " for an indefinite period, the reason: " + reason + "." : "Banned chat to the player " + charName + " for an indefinite period, the reason is: " + reason + ".";
			}
			Log.add(adminName + " banned the chat player " + charName + " for an indefinite period, the reason: " + reason + ".", "banchat", adminChar);
			result = "You have banned the chat player " + charName + " for an indefinite period.";
		}
		else
		{
			if(!(adminChar == null || adminChar.getPlayerAccess().CanUnBanChat || player != null && player.getNoChannel() == 0))
			{
				return "You do not have the right to change the time of the ban.";
			}
			if(adminChar != null && adminChar.getPlayerAccess().BanChatMaxValue != -1 && val > adminChar.getPlayerAccess().BanChatMaxValue)
			{
				return "You can ban no more than " + adminChar.getPlayerAccess().BanChatMaxValue + " minutes.";
			}
			if(Config.BANCHAT_ANNOUNCE)
			{
				announce = Config.BANCHAT_ANNOUNCE_NICK && adminName != null && !adminName.isEmpty() ? adminName + " banned the chat player " + charName + " on " + val + " minutes, the reason: " + reason + "." : "Banned chat to the player " + charName + " on " + val + " minutes, reason: " + reason + ".";
			}
			Log.add(adminName + " banned the chat player " + charName + " on " + val + " minutes, the reason: " + reason + ".", "banchat", adminChar);
			result = "You have banned the chat player " + charName + " on " + val + " minutes.";
		}
		if(player != null)
		{
			updateNoChannel(player, val, reason);
		}
		else
		{
			AutoBan.ChatBan(charName, val, reason, adminName);
		}
		if(announce != null)
		{
			if(Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD)
			{
				Announcements.getInstance().announceToAll(announce);
			}
			else if(adminChar != null)
			{
				Announcements.shout(adminChar, announce, ChatType.CRITICAL_ANNOUNCE);
			}
		}
		return result;
	}
	
	private static void updateNoChannel(Player player, int time, String reason)
	{
		player.updateNoChannel(time * 60000);
		if(time == 0)
		{
			player.sendMessage(new CustomMessage("common.ChatUnBanned", player));
		}
		else if(time > 0)
		{
			if(reason == null || reason.isEmpty())
			{
				player.sendMessage(new CustomMessage("common.ChatBanned", player).addNumber(time));
			}
			else
			{
				player.sendMessage(new CustomMessage("common.ChatBannedWithReason", player).addNumber(time).addString(reason));
			}
		}
		else if(reason == null || reason.isEmpty())
		{
			player.sendMessage(new CustomMessage("common.ChatBannedPermanently", player));
		}
		else
		{
			player.sendMessage(new CustomMessage("common.ChatBannedPermanentlyWithReason", player).addString(reason));
		}
	}
}