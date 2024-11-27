package services;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.Config;
import l2.gameserver.data.StringHolder;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.Player;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2.gameserver.scripts.Functions;
import l2.gameserver.scripts.ScriptFile;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

public class TopPvPPKService extends Functions implements ScriptFile
{
	private static final Logger LOG = LoggerFactory.getLogger(TopPvPPKService.class);
	private static final String SERVICE_HTML_BASE = "scripts/services/";
	
	private static void sendTopRecordHtml(TopRecordHolder topRecordHolder, Player player, NpcInstance npc, String htmlFile)
	{
		NpcHtmlMessage msg = new NpcHtmlMessage(player, npc).setFile(SERVICE_HTML_BASE + htmlFile);
		StringBuilder contentHtmlBuilder = new StringBuilder();
		Collection<TopRecord> topRecords = topRecordHolder.getTopRecords();
		for(TopRecord topRecord : topRecords)
		{
			contentHtmlBuilder.append(topRecord.formatHtml(player));
		}
		msg.replace("%content%", contentHtmlBuilder.toString());
		player.sendPacket(msg);
	}
	
	public void topPvP()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(!Config.SERVICES_PVP_PK_STATISTIC)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
			return;
		}
		sendTopRecordHtml(TopPvPRecordHolder.getInstance(), player, getNpc(), "top_pvp.htm");
	}
	
	public void topPK()
	{
		Player player = getSelf();
		if(player == null)
		{
			return;
		}
		if(!Config.SERVICES_PVP_PK_STATISTIC)
		{
			player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
			return;
		}
		sendTopRecordHtml(TopPKRecordHolder.getInstance(), player, getNpc(), "top_pk.htm");
	}
	
	@Override
	public void onLoad()
	{
	}
	
	@Override
	public void onReload()
	{
	}
	
	@Override
	public void onShutdown()
	{
	}
	
	private static class TopPKRecordHolder extends TopRecordHolder
	{
		private static final TopPKRecordHolder INSTANCE = new TopPKRecordHolder();
		
		private TopPKRecordHolder()
		{
			super(Config.PVP_PK_STAT_RECORD_LIMIT, Config.PVP_PK_STAT_CACHE_UPDATE_INTERVAL);
		}
		
		public static TopPKRecordHolder getInstance()
		{
			return INSTANCE;
		}
		
		@Override
		protected Collection<TopRecord> fetchTopOnlineRecords()
		{
			LinkedList<TopRecord> recordSet = new LinkedList<>();
			for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			{
				recordSet.add(new TopRecord(player.getObjectId(), player.getName(), player.getPkKills()));
			}
			return recordSet;
		}
		
		@Override
		protected Collection<TopRecord> fetchTopDbRecords()
		{
			LinkedList<TopRecord> recordSet = new LinkedList<>();
			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rset = null;
			try
			{
				conn = DatabaseFactory.getInstance().getConnection();
				pstmt = conn.prepareStatement("SELECT  `characters`.`obj_Id` AS `playerObjectId`,  `characters`.`char_name` AS `playerName`,  `characters`.`pkkills` AS `pkKills` FROM  `characters` ORDER BY  `characters`.`pkkills` DESC LIMIT ?");
				pstmt.setInt(1, getLimit());
				rset = pstmt.executeQuery();
				while(rset.next())
				{
					recordSet.add(new TopRecord(rset.getInt("playerObjectId"), rset.getString("playerName"), rset.getInt("pkKills")));
				}
			}
			catch(SQLException e)
			{
				LOG.error("Can't fetch top PK records.", e);
			}
			finally
			{
				DbUtils.closeQuietly(conn, pstmt, rset);
			}
			return recordSet;
		}
	}
	
	private static class TopPvPRecordHolder extends TopRecordHolder
	{
		private static final TopPvPRecordHolder INSTANCE = new TopPvPRecordHolder();
		
		private TopPvPRecordHolder()
		{
			super(Config.PVP_PK_STAT_RECORD_LIMIT, Config.PVP_PK_STAT_CACHE_UPDATE_INTERVAL);
		}
		
		public static TopPvPRecordHolder getInstance()
		{
			return INSTANCE;
		}
		
		@Override
		protected Collection<TopRecord> fetchTopOnlineRecords()
		{
			LinkedList<TopRecord> recordSet = new LinkedList<>();
			for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			{
				recordSet.add(new TopRecord(player.getObjectId(), player.getName(), player.getPvpKills()));
			}
			return recordSet;
		}
		
		@Override
		protected Collection<TopRecord> fetchTopDbRecords()
		{
			LinkedList<TopRecord> recordSet = new LinkedList<>();
			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rset = null;
			try
			{
				conn = DatabaseFactory.getInstance().getConnection();
				pstmt = conn.prepareStatement("SELECT  `characters`.`obj_Id` AS `playerObjectId`,  `characters`.`char_name` AS `playerName`,  `characters`.`pvpkills` AS `pvpKills` FROM  `characters` ORDER BY  `characters`.`pvpkills` DESC LIMIT ?");
				pstmt.setInt(1, getLimit());
				rset = pstmt.executeQuery();
				while(rset.next())
				{
					recordSet.add(new TopRecord(rset.getInt("playerObjectId"), rset.getString("playerName"), rset.getInt("pvpKills")));
				}
			}
			catch(SQLException e)
			{
				LOG.error("Can't fetch top PvP records.", e);
			}
			finally
			{
				DbUtils.closeQuietly(conn, pstmt, rset);
			}
			return recordSet;
		}
	}
	
	private abstract static class TopRecordHolder
	{
		private final AtomicReference<Pair<Long, Collection<TopRecord>>> _lastUpdateAndRecords;
		private final int _limit;
		private final long _updateInterval;
		
		protected TopRecordHolder(int limit, long updateInterval)
		{
			_updateInterval = updateInterval;
			_lastUpdateAndRecords = new AtomicReference<>(Pair.of(0L, Collections.emptyList()));
			_limit = limit;
		}
		
		protected int getLimit()
		{
			return _limit;
		}
		
		protected abstract Collection<TopRecord> fetchTopOnlineRecords();
		
		protected abstract Collection<TopRecord> fetchTopDbRecords();
		
		protected Collection<TopRecord> fetchTopRecords()
		{
			Collection<TopRecord> onlineRecords = fetchTopOnlineRecords();
			Collection<TopRecord> dbRecords = fetchTopDbRecords();
			ArrayList<TopRecord> result = new ArrayList<>(onlineRecords.size() + dbRecords.size());
			for(TopRecord topRecord : onlineRecords)
			{
				result.add(topRecord);
			}
			for(TopRecord topRecord : dbRecords)
			{
				if(result.contains(topRecord))
					continue;
				result.add(topRecord);
			}
			Collections.sort(result);
			return new ArrayList<>(result.subList(0, Math.min(_limit, result.size())));
		}
		
		public Collection<TopRecord> getTopRecords()
		{
			Pair<Long, Collection<TopRecord>> lastUpdateAndRecords;
			while((lastUpdateAndRecords = _lastUpdateAndRecords.get()).getLeft() + _updateInterval < System.currentTimeMillis())
			{
				Collection<TopRecord> newTopRecords = fetchTopRecords();
				Pair newLastUpdateAndRecords = Pair.of((Object) System.currentTimeMillis(), newTopRecords);
				if(!_lastUpdateAndRecords.compareAndSet(lastUpdateAndRecords, newLastUpdateAndRecords))
					continue;
				return Collections.unmodifiableCollection((Collection) newLastUpdateAndRecords.getRight());
			}
			return Collections.unmodifiableCollection((Collection) lastUpdateAndRecords.getRight());
		}
	}
	
	private static class TopRecord implements Comparable<TopRecord>
	{
		private final int _playerObjectId;
		private final int _value;
		protected String _playerName;
		
		private TopRecord(int playerObjectId, String playerName, int value)
		{
			_playerObjectId = playerObjectId;
			_playerName = playerName;
			_value = value;
		}
		
		protected int getPlayerObjectId()
		{
			return _playerObjectId;
		}
		
		protected Player getPlayer()
		{
			return GameObjectsStorage.getPlayer(_playerObjectId);
		}
		
		public String getPlayerName()
		{
			return _playerName;
		}
		
		public boolean isOnline()
		{
			Player player = getPlayer();
			return player != null && player.isOnline();
		}
		
		public int getTopValue()
		{
			return _value;
		}
		
		public String formatHtml(Player forPlayer)
		{
			String html = StringHolder.getInstance().getNotNull(forPlayer, isOnline() ? "services.TopPvPPKService.TopRecord.RecordHtmlPlayerOnline" : "services.TopPvPPKService.TopRecord.RecordHtmlPlayerOffline");
			html = html.replace("%name%", getPlayerName());
			html = html.replace("%val%", String.valueOf(getTopValue()));
			return html;
		}
		
		@Override
		public int compareTo(TopRecord o)
		{
			if(getPlayerObjectId() == o.getPlayerObjectId())
			{
				return 0;
			}
			return o.getTopValue() - getTopValue();
		}
		
		@Override
		public boolean equals(Object o)
		{
			if(this == o)
			{
				return true;
			}
			if(o == null || !(o instanceof TopRecord))
			{
				return false;
			}
			return getPlayerObjectId() == ((TopRecord) o).getPlayerObjectId();
		}
	}
}