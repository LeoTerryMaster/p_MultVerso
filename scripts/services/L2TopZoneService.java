package services;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.Config;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.GameClient;
import l2.gameserver.network.l2.components.SystemMsg;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.scripts.ScriptFile;
import l2.gameserver.utils.ItemFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class L2TopZoneService implements ScriptFile, IVoicedCommandHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(L2TopZoneService.class);
	private static final String[] COMMAND_LIST = {"l2topzone", "topzone"};
	private static final String URL_TEMPLATE = "http://l2topzone.com/api.php?API_KEY=%api_key%&SERVER_ID=%server_id%&IP=%player_key%";
	private static final L2TopZoneService INSTANCE = new L2TopZoneService();
	
	public static L2TopZoneService getInstance()
	{
		return INSTANCE;
	}
	
	private static String getPlayerKey(Player player)
	{
		if(player == null)
		{
			return null;
		}
		GameClient client = player.getNetConnection();
		if(client == null || !client.isConnected() || client.getState() != GameClient.GameClientState.IN_GAME)
		{
			return null;
		}
		return client.getIpAddr();
	}
	
	private static long getLastStoredCheckDate(String playerKey)
	{
		long result = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			pstmt = conn.prepareStatement("SELECT `last_check` AS `lastCheck` FROM `l2topzone_votes` WHERE `player_key` = ?");
			pstmt.setString(1, playerKey);
			rset = pstmt.executeQuery();
			if(rset.next())
			{
				result = rset.getLong(1);
			}
		}
		catch(SQLException e)
		{
			LOG.error("L2TopZoneService: Cant get last stored vote check date", e);
		}
		finally
		{
			DbUtils.closeQuietly(conn, pstmt, rset);
		}
		return result;
	}
	
	private static void setLastStoredCheckDate(String playerKey, long lastCheck)
	{
		Connection conn = null;
		PreparedStatement pstmt = null;
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			pstmt = conn.prepareStatement("REPLACE INTO `l2topzone_votes`(`player_key`, `last_check`) VALUES (?, ?)");
			pstmt.setString(1, playerKey);
			pstmt.setLong(2, lastCheck);
			pstmt.executeUpdate();
		}
		catch(SQLException e)
		{
			LOG.error("L2TopZoneService: Cant set last stored vote check date", e);
		}
		finally
		{
			DbUtils.closeQuietly(conn, pstmt);
		}
	}
	
	private static boolean requestCheckVote(String apiKey, int serverId, String playerKey) throws Exception
	{
		InputStream is;
		boolean bl;
		block:
		{
			URL url = new URL("http://l2topzone.com/api.php?API_KEY=%api_key%&SERVER_ID=%server_id%&IP=%player_key%".replace("%api_key%", apiKey).replace("%server_id%", String.valueOf(serverId)).replace("%player_key%", playerKey));
			URLConnection conn = url.openConnection();
			conn.addRequestProperty("Host", url.getHost());
			conn.addRequestProperty("Accept", "*/*");
			conn.addRequestProperty("Connection", "close");
			conn.addRequestProperty("User-Agent", "L2TopZone");
			conn.setConnectTimeout(5000);
			is = null;
			InputStreamReader isr = null;
			try
			{
				is = conn.getInputStream();
				StringBuilder sb = new StringBuilder();
				isr = new InputStreamReader(is);
				int len;
				char[] buff = new char[64];
				while((len = isr.read(buff)) > 0 && sb.length() < 1024)
				{
					sb.append(buff, 0, len);
				}
				bl = Boolean.parseBoolean(sb.toString());
				if(isr == null)
					break block;
			}
			catch(Throwable e)
			{
				try
				{
					if(isr != null)
					{
						try
						{
							isr.close();
						}
						catch(IOException e2)
						{
							
						}
					}
					if(is != null)
					{
						try
						{
							is.close();
						}
						catch(IOException e2)
						{
							
						}
					}
					throw e;
				}
				catch(Exception e2)
				{
					LOG.warn("L2TopZoneService: Check request failed", e);
					return false;
				}
			}
			try
			{
				isr.close();
			}
			catch(IOException e)
			{
				
			}
		}
		if(is != null)
		{
			try
			{
				is.close();
			}
			catch(IOException e)
			{
				
			}
		}
		return bl;
	}
	
	private void processPlayerRewardRequest(Player player)
	{
		String playerKey = getPlayerKey(player);
		if(player == null)
		{
			return;
		}
		long now = System.currentTimeMillis() / 1000;
		long lastStoredCheckDate = getLastStoredCheckDate(playerKey);
		long checkRemainingTime = lastStoredCheckDate + Config.L2TOPZONE_VOTE_TIME_TO_LIVE - now;
		if(checkRemainingTime > 0)
		{
			int hours = (int) (checkRemainingTime / 3600);
			int minuter = (int) (checkRemainingTime % 3600);
			if(hours > 0)
			{
				player.sendPacket(new SystemMessage(1813).addString("L2TopZone").addNumber(hours));
			}
			else
			{
				player.sendPacket(new SystemMessage(1814).addString("L2TopZone").addNumber(minuter));
			}
			return;
		}
		setLastStoredCheckDate(playerKey, now);
		try
		{
			if(requestCheckVote(Config.L2TOPZONE_API_KEY, Config.L2TOPZONE_SERVER_ID, playerKey))
			{
				ItemFunctions.addItem(player, Config.L2TOPZONE_REWARD_ITEM_ID, (long) Config.L2TOPZONE_REWARD_ITEM_COUNT, true);
				player.sendMessage("L2TopZone: Thank you for your vote.");
			}
			else
			{
				player.sendMessage("L2TopZone: Vote first!");
			}
		}
		catch(Exception e)
		{
			LOG.warn("L2TopZoneService: Cant process reward.");
			player.sendPacket(SystemMsg.SYSTEM_ERROR);
		}
	}
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		for(String c : COMMAND_LIST)
		{
			if(!c.equalsIgnoreCase(command))
				continue;
			processPlayerRewardRequest(activeChar);
			return true;
		}
		return false;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return COMMAND_LIST;
	}
	
	@Override
	public void onLoad()
	{
		if(Config.L2TOPZONE_ENABLED)
		{
			VoicedCommandHandler.getInstance().registerVoicedCommandHandler(getInstance());
		}
	}
	
	@Override
	public void onReload()
	{
	}
	
	@Override
	public void onShutdown()
	{
	}
}