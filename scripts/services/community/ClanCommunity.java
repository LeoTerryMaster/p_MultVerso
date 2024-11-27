package services.community;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.Config;
import l2.gameserver.cache.Msg;
import l2.gameserver.data.htm.HtmCache;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.handler.bbs.CommunityBoardManager;
import l2.gameserver.handler.bbs.ICommunityBoardHandler;
import l2.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2.gameserver.model.Player;
import l2.gameserver.model.actor.listener.CharListenerList;
import l2.gameserver.model.pledge.Clan;
import l2.gameserver.model.pledge.UnitMember;
import l2.gameserver.network.l2.s2c.ExMailArrived;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.network.l2.s2c.ShowBoard;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.tables.ClanTable;
import l2.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class ClanCommunity extends Functions implements ScriptFile, ICommunityBoardHandler
{
	private static final Logger _log = LoggerFactory.getLogger(ClanCommunity.class);
	private static final int CLANS_PER_PAGE = 10;
	private final Listener _listener = new Listener();
	
	private static List<Clan> getClanList(String search, boolean byCL)
	{
		Clan[] clans = ClanTable.getInstance().getClans();
		Arrays.sort(clans, new ClansComparator());
		Clan[] var4 = clans;
		int var5 = clans.length;
		
		ArrayList<Clan> clanList = new ArrayList();
		for(int var6 = 0;var6 < var5;++var6)
		{
			Clan clan = var4[var6];
			if(clan.getLevel() > 1)
			{
				clanList.add(clan);
			}
		}
		
		if(!search.isEmpty())
		{
			ArrayList<Clan> searchList = new ArrayList();
			Iterator var9 = clanList.iterator();
			
			while(true)
			{
				while(var9.hasNext())
				{
					Clan clan = (Clan) var9.next();
					if(byCL && clan.getLeaderName().toLowerCase().contains(search.toLowerCase()))
					{
						searchList.add(clan);
					}
					else if(!byCL && clan.getName().toLowerCase().contains(search.toLowerCase()))
					{
						searchList.add(clan);
					}
				}
				
				clanList = searchList;
				break;
			}
		}
		
		return clanList;
	}
	
	@Override
	public void onLoad()
	{
		CharListenerList.addGlobal(_listener);
		if(Config.COMMUNITYBOARD_ENABLED)
		{
			_log.info("CommunityBoard: Clan Community service loaded.");
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}
	
	@Override
	public void onReload()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
		{
			CommunityBoardManager.getInstance().removeHandler(this);
		}
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	@Override
	public String[] getBypassCommands()
	{
		return new String[] {"_bbsclan", "_clbbsclan_", "_clbbslist_", "_clsearch", "_clbbsadmi", "_mailwritepledgeform", "_announcepledgewriteform", "_announcepledgeswitchshowflag", "_announcepledgewrite", "_clwriteintro", "_clwritemail"};
	}
	
	@Override
	public void onBypassCommand(Player player, String bypass)
	{
		StringTokenizer st = new StringTokenizer(bypass, "_");
		String cmd = st.nextToken();
		player.setSessionVar("add_fav", null);
		Clan clan;
		if("bbsclan".equals(cmd))
		{
			clan = player.getClan();
			if(clan != null && clan.getLevel() > 1)
			{
				onBypassCommand(player, "_clbbsclan_" + player.getClanId());
				return;
			}
			
			onBypassCommand(player, "_clbbslist_1_0_");
		}
		else
		{
			int clanId;
			int type;
			String notice;
			if("clbbslist".equals(cmd))
			{
				clanId = Integer.parseInt(st.nextToken());
				type = Integer.parseInt(st.nextToken());
				String search = st.hasMoreTokens() ? st.nextToken() : "";
				HashMap<Integer, String> tpls = Util.parseTemplate(HtmCache.getInstance().getNotNull("scripts/services/community/bbs_clanlist.htm", player));
				String html = tpls.get(0);
				Clan playerClan = player.getClan();
				if(playerClan != null)
				{
					notice = tpls.get(1);
					notice = notice.replace("%PLEDGE_ID%", String.valueOf(playerClan.getClanId()));
					notice = notice.replace("%MY_PLEDGE_NAME%", playerClan.getLevel() > 1 ? playerClan.getName() : "");
					html = html.replace("<?my_clan_link?>", notice);
				}
				else
				{
					html = html.replace("<?my_clan_link?>", "");
				}
				
				List<Clan> clanList = getClanList(search, type == 1);
				type = (clanId - 1) * 10;
				int end = Math.min(clanId * 10, clanList.size());
				int ep;
				if(clanId == 1)
				{
					html = html.replace("%ACTION_GO_LEFT%", "");
					html = html.replace("%GO_LIST%", "");
					html = html.replace("%NPAGE%", "1");
				}
				else
				{
					html = html.replace("%ACTION_GO_LEFT%", "bypass _clbbslist_" + (clanId - 1) + "_" + type + "_" + search);
					html = html.replace("%NPAGE%", String.valueOf(clanId));
					StringBuilder goList = new StringBuilder("");
					
					for(ep = clanId > 10 ? clanId - 10 : 1;ep < clanId;++ep)
					{
						goList.append("<td><a action=\"bypass _clbbslist_").append(ep).append("_").append(type).append("_").append(search).append("\"> ").append(ep).append(" </a> </td>\n\n");
					}
					
					html = html.replace("%GO_LIST%", goList.toString());
				}
				
				int pages = Math.max(clanList.size() / 10, 1);
				if(clanList.size() > pages * 10)
				{
					++pages;
				}
				
				int i;
				if(pages <= clanId)
				{
					html = html.replace("%ACTION_GO_RIGHT%", "");
					html = html.replace("%GO_LIST2%", "");
				}
				else
				{
					html = html.replace("%ACTION_GO_RIGHT%", "bypass _clbbslist_" + (clanId + 1) + "_" + type + "_" + search);
					ep = Math.min(clanId + 10, pages);
					StringBuilder goList = new StringBuilder("");
					
					for(i = clanId + 1;i <= ep;++i)
					{
						goList.append("<td><a action=\"bypass _clbbslist_").append(i).append("_").append(type).append("_").append(search).append("\"> ").append(i).append(" </a> </td>\n\n");
					}
					
					html = html.replace("%GO_LIST2%", goList.toString());
				}
				
				StringBuilder cl = new StringBuilder("");
				String tpl = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_clantpl.htm", player);
				
				for(i = type;i < end;++i)
				{
					clan = clanList.get(i);
					String clantpl = tpl.replace("%action_clanhome%", "bypass _clbbsclan_" + clan.getClanId());
					clantpl = clantpl.replace("%clan_name%", clan.getName());
					clantpl = clantpl.replace("%clan_owner%", clan.getLeaderName());
					clantpl = clantpl.replace("%skill_level%", String.valueOf(clan.getLevel()));
					clantpl = clantpl.replace("%member_count%", String.valueOf(clan.getAllSize()));
					cl.append(clantpl);
				}
				
				html = html.replace("%CLAN_LIST%", cl.toString());
				ShowBoard.separateAndSend(html, player);
			}
			else
			{
				ResultSet rset = null;
				Connection con = null;
				String intro;
				PreparedStatement statement = null;
				if("clbbsclan".equals(cmd))
				{
					clanId = Integer.parseInt(st.nextToken());
					if(clanId == 0)
					{
						player.sendPacket(new SystemMessage(238));
						onBypassCommand(player, "_clbbslist_1_0");
						return;
					}
					
					clan = ClanTable.getInstance().getClan(clanId);
					if(clan == null)
					{
						onBypassCommand(player, "_clbbslist_1_0");
						return;
					}
					
					if(clan.getLevel() < 2)
					{
						player.sendPacket(new SystemMessage(1050));
						onBypassCommand(player, "_clbbslist_1_0");
						return;
					}
					
					intro = "";
					
					try
					{
						con = DatabaseFactory.getInstance().getConnection();
						statement = con.prepareStatement("SELECT * FROM `bbs_clannotice` WHERE `clan_id` = ? and type = 2");
						statement.setInt(1, clanId);
						rset = statement.executeQuery();
						if(rset.next())
						{
							intro = rset.getString("notice");
						}
					}
					catch(Exception var62)
					{
					}
					finally
					{
						DbUtils.closeQuietly(con, statement, rset);
					}
					
					HashMap<Integer, String> tpls = Util.parseTemplate(HtmCache.getInstance().getNotNull("scripts/services/community/bbs_clan.htm", player));
					String html = tpls.get(0);
					html = html.replace("%PLEDGE_ID%", String.valueOf(clanId));
					html = html.replace("%ACTION_ANN%", "");
					html = html.replace("%ACTION_FREE%", "");
					if(player.getClanId() == clanId && player.isClanLeader())
					{
						html = html.replace("<?menu?>", tpls.get(1));
					}
					else
					{
						html = html.replace("<?menu?>", "");
					}
					
					html = html.replace("%CLAN_INTRO%", intro.replace("\n", "<br1>"));
					html = html.replace("%CLAN_NAME%", clan.getName());
					html = html.replace("%SKILL_LEVEL%", String.valueOf(clan.getLevel()));
					html = html.replace("%CLAN_MEMBERS%", String.valueOf(clan.getAllSize()));
					html = html.replace("%OWNER_NAME%", clan.getLeaderName());
					html = html.replace("%ALLIANCE_NAME%", clan.getAlliance() != null ? clan.getAlliance().getAllyName() : "");
					html = html.replace("%ANN_LIST%", "");
					html = html.replace("%THREAD_LIST%", "");
					ShowBoard.separateAndSend(html, player);
				}
				else
				{
					String html;
					if("clbbsadmi".equals(cmd))
					{
						clan = player.getClan();
						if(clan == null || clan.getLevel() < 2 || !player.isClanLeader())
						{
							onBypassCommand(player, "_clbbsclan_" + player.getClanId());
							return;
						}
						
						html = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_clanadmin.htm", player);
						html = html.replace("%PLEDGE_ID%", String.valueOf(clan.getClanId()));
						html = html.replace("%ACTION_ANN%", "");
						html = html.replace("%ACTION_FREE%", "");
						html = html.replace("%CLAN_NAME%", clan.getName());
						html = html.replace("%per_list%", "");
						con = null;
						statement = null;
						rset = null;
						intro = "";
						
						try
						{
							con = DatabaseFactory.getInstance().getConnection();
							statement = con.prepareStatement("SELECT * FROM `bbs_clannotice` WHERE `clan_id` = ? and type = 2");
							statement.setInt(1, clan.getClanId());
							rset = statement.executeQuery();
							if(rset.next())
							{
								intro = rset.getString("notice");
							}
						}
						catch(Exception var60)
						{
						}
						finally
						{
							DbUtils.closeQuietly(con, statement, rset);
						}
						
						List<String> args = new ArrayList();
						args.add("0");
						args.add("0");
						args.add("0");
						args.add("0");
						args.add("0");
						args.add("0");
						args.add("");
						args.add("0");
						args.add("");
						args.add("0");
						args.add("");
						args.add("");
						args.add(intro);
						args.add("");
						args.add("");
						args.add("0");
						args.add("0");
						args.add("");
						player.sendPacket(new ShowBoard(html, "1001", player));
						player.sendPacket(new ShowBoard(args));
					}
					else if("mailwritepledgeform".equals(cmd))
					{
						clan = player.getClan();
						if(clan == null || clan.getLevel() < 2 || !player.isClanLeader())
						{
							onBypassCommand(player, "_clbbsclan_" + player.getClanId());
							return;
						}
						
						html = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_pledge_mail_write.htm", player);
						html = html.replace("%PLEDGE_ID%", String.valueOf(clan.getClanId()));
						html = html.replace("%pledge_id%", String.valueOf(clan.getClanId()));
						html = html.replace("%pledge_name%", clan.getName());
						ShowBoard.separateAndSend(html, player);
					}
					else if("announcepledgewriteform".equals(cmd))
					{
						clan = player.getClan();
						if(clan == null || clan.getLevel() < 2 || !player.isClanLeader())
						{
							onBypassCommand(player, "_clbbsclan_" + player.getClanId());
							return;
						}
						
						HashMap<Integer, String> tpls = Util.parseTemplate(HtmCache.getInstance().getNotNull("scripts/services/community/bbs_clanannounce.htm", player));
						html = tpls.get(0);
						html = html.replace("%PLEDGE_ID%", String.valueOf(clan.getClanId()));
						html = html.replace("%ACTION_ANN%", "");
						html = html.replace("%ACTION_FREE%", "");
						notice = "";
						type = 0;
						
						try
						{
							con = DatabaseFactory.getInstance().getConnection();
							statement = con.prepareStatement("SELECT * FROM `bbs_clannotice` WHERE `clan_id` = ? and type != 2");
							statement.setInt(1, clan.getClanId());
							rset = statement.executeQuery();
							if(rset.next())
							{
								notice = rset.getString("notice");
								type = rset.getInt("type");
							}
						}
						catch(Exception var58)
						{
						}
						finally
						{
							DbUtils.closeQuietly(con, statement, rset);
						}
						
						if(type == 0)
						{
							html = html.replace("<?usage?>", tpls.get(1));
						}
						else
						{
							html = html.replace("<?usage?>", tpls.get(2));
						}
						
						html = html.replace("%flag%", String.valueOf(type));
						List<String> args = new ArrayList();
						args.add("0");
						args.add("0");
						args.add("0");
						args.add("0");
						args.add("0");
						args.add("0");
						args.add("");
						args.add("0");
						args.add("");
						args.add("0");
						args.add("");
						args.add("");
						args.add(notice);
						args.add("");
						args.add("");
						args.add("0");
						args.add("0");
						args.add("");
						player.sendPacket(new ShowBoard(html, "1001", player));
						player.sendPacket(new ShowBoard(args));
					}
					else if("announcepledgeswitchshowflag".equals(cmd))
					{
						clan = player.getClan();
						if(clan == null || clan.getLevel() < 2 || !player.isClanLeader())
						{
							onBypassCommand(player, "_clbbsclan_" + player.getClanId());
							return;
						}
						
						type = Integer.parseInt(st.nextToken());
						con = null;
						statement = null;
						
						try
						{
							con = DatabaseFactory.getInstance().getConnection();
							statement = con.prepareStatement("UPDATE `bbs_clannotice` SET type = ? WHERE `clan_id` = ? and type = ?");
							statement.setInt(1, type);
							statement.setInt(2, clan.getClanId());
							statement.setInt(3, type == 1 ? 0 : 1);
							statement.execute();
						}
						catch(Exception var56)
						{
						}
						finally
						{
							DbUtils.closeQuietly(con, statement);
						}
						
						clan.setNotice(type == 0 ? "" : null);
						onBypassCommand(player, "_announcepledgewriteform");
					}
				}
			}
		}
	}
	
	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		StringTokenizer st = new StringTokenizer(bypass, "_");
		String cmd = st.nextToken();
		if("clsearch".equals(cmd))
		{
			if(arg3 == null)
			{
				arg3 = "";
			}
			
			onBypassCommand(player, "_clbbslist_1_" + ("Ruler".equals(arg4) ? "1" : "0") + "_" + arg3);
		}
		else
		{
			Connection con = null;
			Clan clan;
			PreparedStatement statement = null;
			if("clwriteintro".equals(cmd))
			{
				clan = player.getClan();
				if(clan == null || clan.getLevel() < 2 || !player.isClanLeader() || arg3 == null || arg3.isEmpty())
				{
					onBypassCommand(player, "_clbbsclan_" + player.getClanId());
					return;
				}
				
				arg3 = arg3.replace("<", "");
				arg3 = arg3.replace(">", "");
				arg3 = arg3.replace("&", "");
				arg3 = arg3.replace("$", "");
				arg3 = arg3.replace("\"", "&quot;");
				if(arg3.length() > 3000)
				{
					arg3 = arg3.substring(0, 3000);
				}
				
				con = null;
				statement = null;
				
				try
				{
					con = DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement("REPLACE INTO `bbs_clannotice`(clan_id, type, notice) VALUES(?, ?, ?)");
					statement.setInt(1, clan.getClanId());
					statement.setInt(2, 2);
					statement.setString(3, arg3);
					statement.execute();
				}
				catch(Exception var35)
				{
				}
				finally
				{
					DbUtils.closeQuietly(con, statement);
				}
				
				onBypassCommand(player, "_clbbsclan_" + player.getClanId());
			}
			else if("clwritemail".equals(cmd))
			{
				clan = player.getClan();
				if(clan == null || clan.getLevel() < 2 || !player.isClanLeader())
				{
					onBypassCommand(player, "_clbbsclan_" + player.getClanId());
					return;
				}
				
				if(arg3 == null || arg4 == null)
				{
					player.sendPacket(Msg.THE_MESSAGE_WAS_NOT_SENT);
					onBypassCommand(player, "_clbbsclan_" + player.getClanId());
					return;
				}
				
				arg3 = arg3.replace("<", "");
				arg3 = arg3.replace(">", "");
				arg3 = arg3.replace("&", "");
				arg3 = arg3.replace("$", "");
				arg3 = arg3.replace("\"", "&quot;");
				arg5 = arg5.replace("<", "");
				arg5 = arg5.replace(">", "");
				arg5 = arg5.replace("&", "");
				arg5 = arg5.replace("$", "");
				arg5 = arg5.replace("\"", "&quot;");
				if(arg3.isEmpty() || arg4.isEmpty())
				{
					player.sendPacket(Msg.THE_MESSAGE_WAS_NOT_SENT);
					onBypassCommand(player, "_clbbsclan_" + player.getClanId());
					return;
				}
				
				if(arg3.length() > 128)
				{
					arg3 = arg3.substring(0, 128);
				}
				
				if(arg4.length() > 3000)
				{
					arg5 = arg5.substring(0, 3000);
				}
				
				con = null;
				statement = null;
				
				Iterator var43;
				try
				{
					con = DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement("INSERT INTO `bbs_mail`(to_name, to_object_id, from_name, from_object_id, title, message, post_date, box_type) VALUES(?, ?, ?, ?, ?, ?, ?, 0)");
					var43 = clan.iterator();
					
					while(true)
					{
						if(!var43.hasNext())
						{
							statement.close();
							statement = con.prepareStatement("INSERT INTO `bbs_mail`(to_name, to_object_id, from_name, from_object_id, title, message, post_date, box_type) VALUES(?, ?, ?, ?, ?, ?, ?, 1)");
							statement.setString(1, clan.getName());
							statement.setInt(2, player.getObjectId());
							statement.setString(3, player.getName());
							statement.setInt(4, player.getObjectId());
							statement.setString(5, arg3);
							statement.setString(6, arg5);
							statement.setInt(7, (int) (System.currentTimeMillis() / 1000L));
							statement.execute();
							break;
						}
						
						UnitMember clm = (UnitMember) var43.next();
						statement.setString(1, clan.getName());
						statement.setInt(2, clm.getObjectId());
						statement.setString(3, player.getName());
						statement.setInt(4, player.getObjectId());
						statement.setString(5, arg3);
						statement.setString(6, arg5);
						statement.setInt(7, (int) (System.currentTimeMillis() / 1000L));
						statement.execute();
					}
				}
				catch(Exception var37)
				{
					player.sendPacket(Msg.THE_MESSAGE_WAS_NOT_SENT);
					onBypassCommand(player, "_clbbsclan_" + player.getClanId());
					return;
				}
				finally
				{
					DbUtils.closeQuietly(con, statement);
				}
				
				player.sendPacket(Msg.YOUVE_SENT_MAIL);
				var43 = clan.getOnlineMembers(0).iterator();
				
				while(var43.hasNext())
				{
					Player member = (Player) var43.next();
					member.sendPacket(Msg.YOUVE_GOT_MAIL);
					member.sendPacket(ExMailArrived.STATIC);
				}
				
				onBypassCommand(player, "_clbbsclan_" + player.getClanId());
			}
			else if("announcepledgewrite".equals(cmd))
			{
				clan = player.getClan();
				if(clan == null || clan.getLevel() < 2 || !player.isClanLeader())
				{
					onBypassCommand(player, "_clbbsclan_" + player.getClanId());
					return;
				}
				
				if(arg3 == null || arg3.isEmpty())
				{
					onBypassCommand(player, "_announcepledgewriteform");
					return;
				}
				
				arg3 = arg3.replace("<", "");
				arg3 = arg3.replace(">", "");
				arg3 = arg3.replace("&", "");
				arg3 = arg3.replace("$", "");
				arg3 = arg3.replace("\"", "&quot;");
				if(arg3.isEmpty())
				{
					onBypassCommand(player, "_announcepledgewriteform");
					return;
				}
				
				if(arg3.length() > 3000)
				{
					arg3 = arg3.substring(0, 3000);
				}
				
				int type = Integer.parseInt(st.nextToken());
				
				try
				{
					con = DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement("REPLACE INTO `bbs_clannotice`(clan_id, type, notice) VALUES(?, ?, ?)");
					statement.setInt(1, clan.getClanId());
					statement.setInt(2, type);
					statement.setString(3, arg3);
					statement.execute();
				}
				catch(Exception var39)
				{
					var39.printStackTrace();
					onBypassCommand(player, "_announcepledgewriteform");
					return;
				}
				finally
				{
					DbUtils.closeQuietly(con, statement);
				}
				
				if(type == 1)
				{
					clan.setNotice(arg3.replace("\n", "<br1>"));
				}
				else
				{
					clan.setNotice("");
				}
				
				player.sendPacket(Msg.NOTICE_HAS_BEEN_SAVED);
				onBypassCommand(player, "_announcepledgewriteform");
			}
		}
	}
	
	private static class ClansComparator<T> implements Comparator<T>
	{
		@Override
		public int compare(Object o1, Object o2)
		{
			if(o1 instanceof Clan && o2 instanceof Clan)
			{
				Clan p1 = (Clan) o1;
				Clan p2 = (Clan) o2;
				return p1.getName().compareTo(p2.getName());
			}
			else
			{
				return 0;
			}
		}
	}
	
	private class Listener implements OnPlayerEnterListener
	{
		@Override
		public void onPlayerEnter(Player player)
		{
			Clan clan = player.getClan();
			if(clan != null && clan.getLevel() >= 2)
			{
				if(clan.getNotice() == null)
				{
					Connection con = null;
					PreparedStatement statement = null;
					ResultSet rset = null;
					String notice = "";
					int type = 0;
					
					try
					{
						con = DatabaseFactory.getInstance().getConnection();
						statement = con.prepareStatement("SELECT * FROM `bbs_clannotice` WHERE `clan_id` = ? and type != 2");
						statement.setInt(1, clan.getClanId());
						rset = statement.executeQuery();
						if(rset.next())
						{
							notice = rset.getString("notice");
							type = rset.getInt("type");
						}
					}
					catch(Exception var12)
					{
					}
					finally
					{
						DbUtils.closeQuietly(con, statement, rset);
					}
					
					clan.setNotice(type == 1 ? notice.replace("\n", "<br1>\n") : "");
				}
				
				if(!clan.getNotice().isEmpty())
				{
					String html = HtmCache.getInstance().getNotNull("scripts/services/community/clan_popup.htm", player);
					html = html.replace("%pledge_name%", clan.getName());
					html = html.replace("%content%", clan.getNotice());
					player.sendPacket(new NpcHtmlMessage(0).setHtml(html));
				}
			}
		}
	}
}