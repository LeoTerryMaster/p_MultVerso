package l2.gameserver.handler.admincommands.impl;

import l2.gameserver.Announcements;
import l2.gameserver.Config;
import l2.gameserver.handler.admincommands.IAdminCommandHandler;
import l2.gameserver.instancemanager.ReflectionManager;
import l2.gameserver.model.Player;
import l2.gameserver.model.World;
import l2.gameserver.model.items.ManufactureItem;
import l2.gameserver.model.items.TradeItem;
import l2.gameserver.network.authcomm.AuthServerCommunication;
import l2.gameserver.network.authcomm.gs2as.ChangeAccessLevel;
import l2.gameserver.network.l2.CGMHelper;
import l2.gameserver.network.l2.GameClient;
import l2.gameserver.network.l2.components.ChatType;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.utils.AdminFunctions;
import l2.gameserver.utils.AutoBan;
import l2.gameserver.utils.Location;
import l2.gameserver.utils.Log;

import java.util.List;
import java.util.StringTokenizer;

public class AdminBan implements IAdminCommandHandler
{
	private static String tradeToString(Player targ, int trade)
	{
		switch(trade)
		{
			case 3:
			{
				List<TradeItem> list = targ.getBuyList();
				if(list == null || list.isEmpty())
				{
					return "";
				}
				String ret = ":buy:";
				for(TradeItem i : list)
				{
					ret = ret + i.getItemId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
				}
				return ret;
			}
			case 1:
			case 8:
			{
				List<TradeItem> list = targ.getSellList();
				if(list == null || list.isEmpty())
				{
					return "";
				}
				String ret = ":sell:";
				for(TradeItem i : list)
				{
					ret = ret + i.getItemId() + ";" + i.getCount() + ";" + i.getOwnersPrice() + ":";
				}
				return ret;
			}
			case 5:
			{
				List<ManufactureItem> list = targ.getCreateList();
				if(list == null || list.isEmpty())
				{
					return "";
				}
				String ret = ":mf:";
				for(ManufactureItem i : list)
				{
					ret = ret + i.getRecipeId() + ";" + i.getCost() + ":";
				}
				return ret;
			}
		}
		return "";
	}
	
	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		StringTokenizer st = new StringTokenizer(fullString);
		if(activeChar.getPlayerAccess().CanTradeBanUnban)
		{
			switch(command)
			{
				case admin_trade_ban:
				{
					return tradeBan(st, activeChar);
				}
				case admin_trade_unban:
				{
					return tradeUnban(st, activeChar);
				}
			}
		}
		if(activeChar.getPlayerAccess().CanBan)
		{
			switch(command)
			{
				case admin_ban:
				{
					ban(st, activeChar);
					break;
				}
				case admin_accban:
				{
					st.nextToken();
					int level = 0;
					int banExpire = 0;
					String account = st.nextToken();
					if(st.hasMoreTokens())
					{
						banExpire = (int) (System.currentTimeMillis() / 1000) + Integer.parseInt(st.nextToken()) * 60;
					}
					else
					{
						level = -100;
					}
					AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(account, level, banExpire));
					GameClient client = AuthServerCommunication.getInstance().getAuthedClient(account);
					Player player;
					if(client == null || (player = client.getActiveChar()) == null)
						break;
					player.kick();
					activeChar.sendMessage("Player " + player.getName() + " kicked.");
					break;
				}
				case admin_accunban:
				{
					st.nextToken();
					String account = st.nextToken();
					AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(account, 0, 0));
					break;
				}
				case admin_trade_ban:
				{
					return tradeBan(st, activeChar);
				}
				case admin_trade_unban:
				{
					return tradeUnban(st, activeChar);
				}
				case admin_hwidban:
				{
					try
					{
						st.nextToken();
						String charNameOrHwid = st.nextToken();
						Player target = World.getPlayer(charNameOrHwid);
						String hwid2ban = null;
						String ip = null;
						String account = null;
						String comment = st.nextToken();
						if(target != null)
						{
							if(target.getNetConnection() != null && target.getNetConnection().isConnected())
							{
								hwid2ban = target.getNetConnection().getHwid();
								ip = target.getNetConnection().getIpAddr();
								account = target.getAccountName();
								AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(account, -100, 0));
								target.kick();
								activeChar.sendMessage("Player " + target.getName() + " kicked.");
							}
						}
						else
						{
							hwid2ban = charNameOrHwid;
						}
						if(hwid2ban != null && !hwid2ban.isEmpty())
						{
							CGMHelper.getInstance().addHWIDBan(hwid2ban, ip, account, comment);
							activeChar.sendMessage("You ban hwid " + hwid2ban + ".");
							break;
						}
						activeChar.sendMessage("Such HWID or player not found.");
					}
					catch(Exception e)
					{
						activeChar.sendMessage("Command syntax: //hwidban [char_name|hwid] comment");
					}
					break;
				}
				case admin_chatban:
				{
					try
					{
						st.nextToken();
						String player = st.nextToken();
						String period = st.nextToken();
						String bmsg = "admin_chatban " + player + " " + period + " ";
						String msg = fullString.substring(bmsg.length(), fullString.length());
						if(AutoBan.ChatBan(player, Integer.parseInt(period), msg, activeChar.getName()))
						{
							activeChar.sendMessage("You ban chat for " + player + ".");
							break;
						}
						activeChar.sendMessage("Can't find char " + player + ".");
					}
					catch(Exception e)
					{
						activeChar.sendMessage("Command syntax: //chatban char_name period reason");
					}
					break;
				}
				case admin_chatunban:
				{
					try
					{
						st.nextToken();
						String player = st.nextToken();
						if(AutoBan.ChatUnBan(player, activeChar.getName()))
						{
							activeChar.sendMessage("You unban chat for " + player + ".");
							break;
						}
						activeChar.sendMessage("Can't find char " + player + ".");
					}
					catch(Exception e)
					{
						activeChar.sendMessage("Command syntax: //chatunban char_name");
					}
					break;
				}
				case admin_jail:
				{
					try
					{
						st.nextToken();
						String player = st.nextToken();
						String period = st.nextToken();
						String reason = st.nextToken();
						Player target = World.getPlayer(player);
						if(target != null)
						{
							target.setVar("jailedFrom", "" + target.getX() + ";" + target.getY() + ";" + target.getZ() + ";" + target.getReflectionId(), -1);
							target.setVar("jailed", period, -1);
							target.startUnjailTask(target, Integer.parseInt(period));
							target.teleToLocation(Location.findPointToStay(target, AdminFunctions.JAIL_SPAWN, 50, 200), ReflectionManager.JAIL);
							if(activeChar.isInStoreMode())
							{
								activeChar.setPrivateStoreType(0);
							}
							target.sitDown(null);
							target.block();
							target.sendMessage("You moved to jail, time to escape - " + period + " minutes, reason - " + reason + " .");
							activeChar.sendMessage("You jailed " + player + ".");
							break;
						}
						activeChar.sendMessage("Can't find char " + player + ".");
					}
					catch(Exception e)
					{
						activeChar.sendMessage("Command syntax: //jail char_name period reason");
					}
					break;
				}
				case admin_unjail:
				{
					try
					{
						st.nextToken();
						String player = st.nextToken();
						Player target = World.getPlayer(player);
						if(target != null && target.getVar("jailed") != null)
						{
							String[] re = target.getVar("jailedFrom").split(";");
							target.teleToLocation(Integer.parseInt(re[0]), Integer.parseInt(re[1]), Integer.parseInt(re[2]));
							target.setReflection(re.length > 3 ? Integer.parseInt(re[3]) : 0);
							target.stopUnjailTask();
							target.unsetVar("jailedFrom");
							target.unsetVar("jailed");
							target.unblock();
							target.standUp();
							activeChar.sendMessage("You unjailed " + player + ".");
							break;
						}
						activeChar.sendMessage("Can't find char " + player + ".");
					}
					catch(Exception e)
					{
						activeChar.sendMessage("Command syntax: //unjail char_name");
					}
					break;
				}
				case admin_cban:
				{
					activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/cban.htm"));
					break;
				}
				case admin_permaban:
				{
					if(activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
					{
						Functions.sendDebugMessage(activeChar, "Target should be set and be a player instance");
						return false;
					}
					Player banned = activeChar.getTarget().getPlayer();
					String banaccount = banned.getAccountName();
					AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(banaccount, -100, 0));
					if(banned.isInOfflineMode())
					{
						banned.setOfflineMode(false);
					}
					banned.kick();
					Functions.sendDebugMessage(activeChar, "Player account " + banaccount + " is banned, player " + banned.getName() + " kicked.");
				}
			}
		}
		return true;
	}
	
	private boolean tradeBan(StringTokenizer st, Player activeChar)
	{
		if(activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
		{
			return false;
		}
		st.nextToken();
		Player targ = (Player) activeChar.getTarget();
		long days = -1;
		long time = -1;
		if(st.hasMoreTokens())
		{
			days = Long.parseLong(st.nextToken());
			time = days * 24 * 60 * 60 * 1000 + System.currentTimeMillis();
		}
		targ.setVar("tradeBan", String.valueOf(time), -1);
		String msg = activeChar.getName() + " заблокировал торговлю персонажу " + targ.getName() + (days == -1 ? " на бессрочный период." : new StringBuilder().append(" на ").append(days).append(" дней.").toString());
		Log.add(targ.getName() + ":" + days + tradeToString(targ, targ.getPrivateStoreType()), "tradeBan", activeChar);
		if(targ.isInOfflineMode())
		{
			targ.setOfflineMode(false);
			targ.kick();
		}
		else if(targ.isInStoreMode())
		{
			targ.setPrivateStoreType(0);
			targ.standUp();
			targ.broadcastCharInfo();
			targ.getBuyList().clear();
		}
		if(Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD)
		{
			Announcements.getInstance().announceToAll(msg);
		}
		else
		{
			Announcements.shout(activeChar, msg, ChatType.CRITICAL_ANNOUNCE);
		}
		return true;
	}
	
	private boolean tradeUnban(StringTokenizer st, Player activeChar)
	{
		if(activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
		{
			return false;
		}
		Player targ = (Player) activeChar.getTarget();
		targ.unsetVar("tradeBan");
		if(Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD)
		{
			Announcements.getInstance().announceToAll(activeChar + " разблокировал торговлю персонажу " + targ + ".");
		}
		else
		{
			Announcements.shout(activeChar, activeChar + " разблокировал торговлю персонажу " + targ + ".", ChatType.CRITICAL_ANNOUNCE);
		}
		Log.add(activeChar + " разблокировал торговлю персонажу " + targ + ".", "tradeBan", activeChar);
		return true;
	}
	
	private boolean ban(StringTokenizer st, Player activeChar)
	{
		try
		{
			st.nextToken();
			String player = st.nextToken();
			int time = 0;
			if(st.hasMoreTokens())
			{
				time = Integer.parseInt(st.nextToken());
			}
			String msg = "";
			if(st.hasMoreTokens())
			{
				msg = "admin_ban " + player + " " + time + " ";
				while(st.hasMoreTokens())
				{
					msg = msg + st.nextToken() + " ";
				}
				msg.trim();
			}
			Player plyr;
			if((plyr = World.getPlayer(player)) != null)
			{
				plyr.sendMessage(new CustomMessage("admincommandhandlers.YoureBannedByGM", plyr));
				plyr.setAccessLevel(-100);
				AutoBan.Banned(plyr, time, msg, activeChar.getName());
				plyr.kick();
				activeChar.sendMessage("You banned " + plyr.getName());
			}
			else if(AutoBan.Banned(player, -100, time, msg, activeChar.getName()))
			{
				activeChar.sendMessage("You banned " + player);
			}
			else
			{
				activeChar.sendMessage("Can't find char: " + player);
			}
		}
		catch(Exception e)
		{
			activeChar.sendMessage("Command syntax: //ban char_name days reason");
		}
		return true;
	}
	
	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
	
	private enum Commands
	{
		admin_ban,
		admin_unban,
		admin_hwidban,
		admin_cban,
		admin_chatban,
		admin_chatunban,
		admin_accban,
		admin_accunban,
		admin_trade_ban,
		admin_trade_unban,
		admin_jail,
		admin_unjail,
		admin_permaban;
	}
}