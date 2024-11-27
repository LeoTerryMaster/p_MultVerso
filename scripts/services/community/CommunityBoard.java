package services.community;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.Config;
import l2.gameserver.data.htm.HtmCache;
import l2.gameserver.data.xml.holder.MultiSellHolder;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.handler.bbs.CommunityBoardManager;
import l2.gameserver.handler.bbs.ICommunityBoardHandler;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.s2c.ShowBoard;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.scripts.Scripts;
import l2.gameserver.tables.ClanTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;

public class CommunityBoard implements ScriptFile, ICommunityBoardHandler
{
	private static final Logger _log = LoggerFactory.getLogger(CommunityBoard.class);
	
	@Override
	public void onLoad()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
		{
			_log.info("CommunityBoard: service loaded.");
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
		return new String[] {"_bbshome", "_bbslink", "_bbsmultisell", "_bbspage", "_bbsscripts"};
	}
	
	@Override
	public void onBypassCommand(Player player, String bypass)
	{
		StringTokenizer st = new StringTokenizer(bypass, "_");
		String cmd = st.nextToken();
		String html = "";
		StringTokenizer st2;
		String sBypass;
		if("bbshome".equals(cmd))
		{
			st2 = new StringTokenizer(Config.BBS_DEFAULT, "_");
			sBypass = st2.nextToken();
			if(!sBypass.equals(cmd))
			{
				onBypassCommand(player, Config.BBS_DEFAULT);
				return;
			}
			
			html = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_top.htm", player);
			int favCount = 0;
			Connection con = null;
			PreparedStatement statement = null;
			ResultSet rset = null;
			
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("SELECT count(*) as cnt FROM `bbs_favorites` WHERE `object_id` = ?");
				statement.setInt(1, player.getObjectId());
				rset = statement.executeQuery();
				if(rset.next())
				{
					favCount = rset.getInt("cnt");
				}
			}
			catch(Exception var16)
			{
			}
			finally
			{
				DbUtils.closeQuietly(con, statement, rset);
			}
			
			html = html.replace("<?fav_count?>", String.valueOf(favCount));
			html = html.replace("<?clan_count?>", String.valueOf(ClanTable.getInstance().getClans().length));
			html = html.replace("<?market_count?>", String.valueOf(CommunityBoardManager.getInstance().getIntProperty("col_count")));
		}
		else if("bbslink".equals(cmd))
		{
			html = HtmCache.getInstance().getNotNull("scripts/services/community/bbs_homepage.htm", player);
		}
		else if(bypass.startsWith("_bbspage"))
		{
			String[] b = bypass.split(":");
			sBypass = b[1];
			html = HtmCache.getInstance().getNotNull("scripts/services/community/pages/" + sBypass + ".htm", player);
		}
		else
		{
			String pBypass;
			ICommunityBoardHandler handler;
			if(bypass.startsWith("_bbsmultisell"))
			{
				st2 = new StringTokenizer(bypass, ";");
				String[] mBypass = st2.nextToken().split(":");
				pBypass = st2.hasMoreTokens() ? st2.nextToken() : null;
				if(pBypass != null)
				{
					handler = CommunityBoardManager.getInstance().getCommunityHandler(pBypass);
					if(handler != null)
					{
						handler.onBypassCommand(player, pBypass);
					}
				}
				
				int listId = Integer.parseInt(mBypass[1]);
				MultiSellHolder.getInstance().SeparateAndSend(listId, player, 0.0D);
				return;
			}
			
			if(bypass.startsWith("_bbsscripts"))
			{
				st2 = new StringTokenizer(bypass, ";");
				sBypass = st2.nextToken().substring(12);
				pBypass = st2.hasMoreTokens() ? st2.nextToken() : null;
				if(pBypass != null)
				{
					handler = CommunityBoardManager.getInstance().getCommunityHandler(pBypass);
					if(handler != null)
					{
						handler.onBypassCommand(player, pBypass);
					}
				}
				
				String[] word = sBypass.split("\\s+");
				String[] args = sBypass.substring(word[0].length()).trim().split("\\s+");
				String[] path = word[0].split(":");
				if(path.length != 2)
				{
					return;
				}
				
				Scripts.getInstance().callScripts(player, path[0], path[1], word.length == 1 ? new Object[0] : new Object[] {args});
				return;
			}
		}
		
		ShowBoard.separateAndSend(html, player);
	}
	
	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
	}
}