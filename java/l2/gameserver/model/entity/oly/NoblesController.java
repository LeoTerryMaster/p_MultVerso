package l2.gameserver.model.entity.oly;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.Config;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.Player;
import l2.gameserver.model.base.ClassId;
import l2.gameserver.utils.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NoblesController
{
	private static final Logger _log = LoggerFactory.getLogger(NoblesController.class);
	private static final String GET_ALL_NOBLES = "SELECT `char_id`,`class_id`,`char_name`,`points_current`,`points_past`,`points_pre_past`,`class_free_cnt`,`class_based_cnt`,`team_cnt`,`comp_win`,`comp_loose`,`comp_done` FROM `oly_nobles`";
	private static final String SAVE_NOBLE = "REPLACE INTO `oly_nobles`(`char_id`,`class_id`,`char_name`,`points_current`,`points_past`,`points_pre_past`,`class_free_cnt`,`class_based_cnt`,`team_cnt`,`comp_win`,`comp_loose`,`comp_done`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
	private static final String REMOVE_NOBLE = "DELETE FROM `oly_nobles` WHERE `char_id` = ?";
	private static final NobleRecordCmp NRCmp;
	private static NoblesController _instance;
	
	static
	{
		NRCmp = new NobleRecordCmp();
	}
	
	private final List<NobleRecord> _nobleses = new CopyOnWriteArrayList<>();
	
	private NoblesController()
	{
		LoadNobleses();
		ComputeRanks();
	}
	
	public static final NoblesController getInstance()
	{
		if(_instance == null)
		{
			_instance = new NoblesController();
		}
		return _instance;
	}
	
	public boolean isNobles(int object_id)
	{
		return null != getNobleRecord(object_id);
	}
	
	public boolean isNobles(Player player)
	{
		return getNobleRecord(player.getObjectId()) != null;
	}
	
	public NobleRecord getNobleRecord(int object_id)
	{
		for(NobleRecord nr : _nobleses)
		{
			if(nr == null || nr.char_id != object_id)
				continue;
			return nr;
		}
		return null;
	}
	
	public void renameNoble(int object_id, String newname)
	{
		for(NobleRecord nr : _nobleses)
		{
			if(nr == null || nr.char_id != object_id)
				continue;
			nr.char_name = newname;
			SaveNobleRecord(nr);
			return;
		}
	}
	
	protected Collection<NobleRecord> getNoblesRecords()
	{
		return _nobleses;
	}
	
	public int getPointsOf(int object_id)
	{
		for(NobleRecord nr : _nobleses)
		{
			if(nr == null || nr.char_id != object_id)
				continue;
			return nr.points_current;
		}
		return -1;
	}
	
	public void setPointsOf(int object_id, int points)
	{
		for(NobleRecord nr : _nobleses)
		{
			if(nr == null || nr.char_id != object_id)
				continue;
			nr.points_current = points;
			SaveNobleRecord(nr);
		}
	}
	
	public synchronized void removeNoble(Player noble)
	{
		NobleRecord nobleRecord = null;
		for(NobleRecord nobleRecord2 : _nobleses)
		{
			if(nobleRecord2.char_id != noble.getObjectId())
				continue;
			nobleRecord = nobleRecord2;
		}
		if(nobleRecord == null)
		{
			return;
		}
		Connection conn = null;
		PreparedStatement pstmt = null;
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			pstmt = conn.prepareStatement("DELETE FROM `oly_nobles` WHERE `char_id` = ?");
			pstmt.setInt(1, nobleRecord.char_id);
			pstmt.executeUpdate();
			_nobleses.remove(nobleRecord);
		}
		catch(SQLException e)
		{
			_log.warn("NoblesController: Can't remove nobleses ", e);
		}
		finally
		{
			ResultSet rset = null;
			DbUtils.closeQuietly(conn, pstmt, rset);
		}
	}
	
	public synchronized void addNoble(Player noble)
	{
		List<NobleRecord> list = _nobleses;
		synchronized(list)
		{
			NobleRecord nr = null;
			for(NobleRecord nr2 : _nobleses)
			{
				if(nr2.char_id != noble.getObjectId())
					continue;
				nr = nr2;
			}
			_nobleses.remove(nr);
			if(nr == null)
			{
				int classId = noble.getBaseClassId();
				if(classId < 88)
				{
					for(ClassId id : ClassId.values())
					{
						if(id.level() != 3 || id.getParent(0).getId() != classId)
							continue;
						classId = id.getId();
						break;
					}
				}
				nr = new NobleRecord(noble.getObjectId(), classId, noble.getName(), Config.OLY_SEASON_START_POINTS, 0, 0, 0, 0, 0, 0, 0, 0);
			}
			if(!noble.getName().equals(nr.char_name))
			{
				nr.char_name = noble.getName();
			}
			if(noble.getBaseClassId() != nr.class_id)
			{
				_log.warn("OlympiadController: " + noble.getName() + " got base class " + noble.getBaseClassId() + " but " + nr.class_id + " class nobless");
			}
			_nobleses.add(nr);
			setNobleRecord(nr);
		}
	}
	
	private void setNobleRecord(NobleRecord noble)
	{
		SaveNobleRecord(noble);
	}
	
	public void LoadNobleses()
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rset = null;
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			stmt = conn.createStatement();
			rset = stmt.executeQuery("SELECT `char_id`,`class_id`,`char_name`,`points_current`,`points_past`,`points_pre_past`,`class_free_cnt`,`class_based_cnt`,`team_cnt`,`comp_win`,`comp_loose`,`comp_done` FROM `oly_nobles`");
			while(rset.next())
			{
				NobleRecord nr = new NobleRecord(rset.getInt("char_id"), rset.getInt("class_id"), rset.getString("char_name"), rset.getInt("points_current"), rset.getInt("points_past"), rset.getInt("points_pre_past"), rset.getInt("class_free_cnt"), rset.getInt("class_based_cnt"), rset.getInt("team_cnt"), rset.getInt("comp_win"), rset.getInt("comp_loose"), rset.getInt("comp_done"));
				_nobleses.add(nr);
			}
		}
		catch(SQLException e)
		{
			_log.warn("NoblesController: Can't load nobleses ", e);
		}
		finally
		{
			DbUtils.closeQuietly(conn, stmt, rset);
		}
		_log.info("NoblesController: loaded " + _nobleses.size() + " nobleses.");
	}
	
	public void SaveNobleses()
	{
		Connection con = null;
		PreparedStatement pstmt = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			pstmt = con.prepareStatement("REPLACE INTO `oly_nobles`(`char_id`,`class_id`,`char_name`,`points_current`,`points_past`,`points_pre_past`,`class_free_cnt`,`class_based_cnt`,`team_cnt`,`comp_win`,`comp_loose`,`comp_done`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
			for(NobleRecord noble : _nobleses)
			{
				pstmt.setInt(1, noble.char_id);
				pstmt.setInt(2, noble.class_id);
				pstmt.setString(3, noble.char_name);
				pstmt.setInt(4, noble.points_current);
				pstmt.setInt(5, noble.points_past);
				pstmt.setInt(6, noble.points_pre_past);
				pstmt.setInt(7, noble.class_free_cnt);
				pstmt.setInt(8, noble.class_based_cnt);
				pstmt.setInt(9, noble.team_cnt);
				pstmt.setInt(10, noble.comp_win);
				pstmt.setInt(11, noble.comp_loose);
				pstmt.setInt(12, noble.comp_done);
				pstmt.executeUpdate();
			}
		}
		catch(Exception e)
		{
			_log.warn("Oly: can't save nobleses : ", e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, pstmt);
		}
	}
	
	public void SaveNobleRecord(NobleRecord noble)
	{
		Connection con = null;
		PreparedStatement pstmt = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			pstmt = con.prepareStatement("REPLACE INTO `oly_nobles`(`char_id`,`class_id`,`char_name`,`points_current`,`points_past`,`points_pre_past`,`class_free_cnt`,`class_based_cnt`,`team_cnt`,`comp_win`,`comp_loose`,`comp_done`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
			pstmt.setInt(1, noble.char_id);
			pstmt.setInt(2, noble.class_id);
			pstmt.setString(3, noble.char_name);
			pstmt.setInt(4, noble.points_current);
			pstmt.setInt(5, noble.points_past);
			pstmt.setInt(6, noble.points_pre_past);
			pstmt.setInt(7, noble.class_free_cnt);
			pstmt.setInt(8, noble.class_based_cnt);
			pstmt.setInt(9, noble.team_cnt);
			pstmt.setInt(10, noble.comp_win);
			pstmt.setInt(11, noble.comp_loose);
			pstmt.setInt(12, noble.comp_done);
			pstmt.executeUpdate();
		}
		catch(Exception e)
		{
			_log.warn("Oly: can't save noble " + noble.char_name, e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, pstmt);
		}
	}
	
	public void TransactNewSeason()
	{
		_log.info("NoblesController: Cleanuping last period.");
		for(NobleRecord nr : _nobleses)
		{
			Log.add(String.format("NoblesController: %s(%d) new season clean. points_current=%d|points_past=%d|points_pre_past=%d|comp_done=%d", nr.char_name, nr.char_id, nr.points_current, nr.points_past, nr.points_pre_past, nr.comp_done), "olympiad");
			if(nr.comp_done >= Config.OLY_MIN_NOBLE_COMPS)
			{
				nr.points_past = nr.points_current;
				nr.points_pre_past = nr.points_current;
			}
			else
			{
				nr.points_past = 0;
				nr.points_pre_past = 0;
			}
			nr.comp_done = 0;
			nr.comp_win = 0;
			nr.comp_loose = 0;
			nr.class_based_cnt = 0;
			nr.class_free_cnt = 0;
			nr.team_cnt = 0;
			nr.points_current = Config.OLY_DEFAULT_POINTS;
		}
		SaveNobleses();
	}
	
	public void AddWeaklyBonus()
	{
		for(NobleRecord nr : _nobleses)
		{
			nr.points_current += Config.OLY_WBONUS_POINTS;
			nr.class_based_cnt = 0;
			nr.class_free_cnt = 0;
			nr.team_cnt = 0;
		}
		SaveNobleses();
	}
	
	public String[] getClassLeaders(int cid)
	{
		ArrayList<NobleRecord> tmp = new ArrayList<>();
		for(NobleRecord nr : _nobleses)
		{
			if(nr.class_id != cid || nr.points_pre_past <= 0)
				continue;
			tmp.add(nr);
		}
		NobleRecord[] leader = tmp.toArray(new NobleRecord[tmp.size()]);
		Arrays.sort(leader, NRCmp);
		ArrayList<String> result = new ArrayList<>();
		for(int i = 0;i < leader.length && i < 15;++i)
		{
			if(leader[i] == null)
				continue;
			result.add(leader[i].char_name);
		}
		return result.toArray(new String[result.size()]);
	}
	
	public int getPlayerClassRank(int cid, int playerId)
	{
		ArrayList<NobleRecord> tmp = new ArrayList<>();
		for(NobleRecord nr : _nobleses)
		{
			if(nr.class_id != cid)
				continue;
			tmp.add(nr);
		}
		Collections.sort(tmp, NRCmp);
		for(int i = 0;i < tmp.size();++i)
		{
			if(tmp.get(i) == null || tmp.get(i).char_id != playerId)
				continue;
			return i;
		}
		return -1;
	}
	
	public synchronized void ComputeRanks()
	{
		_log.info("NoblesController: Computing ranks.");
		NobleRecord[] rank_nobleses = _nobleses.toArray(new NobleRecord[_nobleses.size()]);
		Arrays.sort(rank_nobleses, NRCmp);
		int rank0 = (int) Math.round((double) rank_nobleses.length * 0.01);
		int rank1 = (int) Math.round((double) rank_nobleses.length * 0.1);
		int rank2 = (int) Math.round((double) rank_nobleses.length * 0.25);
		int rank3 = (int) Math.round((double) rank_nobleses.length * 0.5);
		if(rank0 == 0)
		{
			rank0 = 1;
			++rank1;
			++rank2;
			++rank3;
		}
		int i;
		for(i = 0;i <= rank0 && i < rank_nobleses.length;++i)
		{
			rank_nobleses[i].rank = 0;
		}
		while(i <= rank1 && i < rank_nobleses.length)
		{
			rank_nobleses[i].rank = 1;
			++i;
		}
		while(i <= rank2 && i < rank_nobleses.length)
		{
			rank_nobleses[i].rank = 2;
			++i;
		}
		while(i <= rank3 && i < rank_nobleses.length)
		{
			rank_nobleses[i].rank = 3;
			++i;
		}
		while(i < rank_nobleses.length)
		{
			rank_nobleses[i].rank = 4;
			++i;
		}
	}
	
	public synchronized int getNoblessePasses(Player player)
	{
		int coid = player.getObjectId();
		NobleRecord nr = getInstance().getNobleRecord(coid);
		if(nr != null)
		{
			if(nr.points_past == 0)
			{
				return 0;
			}
			int points = 0;
			if(nr.rank >= 0 && nr.rank < Config.OLY_POINTS_SETTLEMENT.length)
			{
				points = Config.OLY_POINTS_SETTLEMENT[nr.rank];
			}
			if(HeroController.getInstance().isCurrentHero(coid) || HeroController.getInstance().isInactiveHero(coid))
			{
				points += Config.OLY_HERO_POINT_BONUS;
			}
			nr.points_past = 0;
			getInstance().SaveNobleRecord(nr);
			return points * Config.OLY_ITEMS_SETTLEMENT_PER_POINT;
		}
		return 0;
	}
	
	private static class NobleRecordCmp implements Comparator<NobleRecord>
	{
		@Override
		public int compare(NobleRecord o1, NobleRecord o2)
		{
			if(o1 == null && o2 == null)
			{
				return 0;
			}
			if(o1 == null && o2 != null)
			{
				return 1;
			}
			if(o2 == null && o1 != null)
			{
				return -1;
			}
			return o2.points_pre_past - o1.points_pre_past;
		}
	}
	
	public class NobleRecord
	{
		public int char_id;
		public int class_id;
		public String char_name;
		public int points_current;
		public int points_past;
		public int points_pre_past;
		public int class_free_cnt;
		public int class_based_cnt;
		public int team_cnt;
		public int comp_win;
		public int comp_loose;
		public int comp_done;
		public int rank;
		
		private NobleRecord(int _char_id, int _class_id, String _char_name, int _points_current, int _points_past, int _points_pre_past, int _class_free_cnt, int _class_based_cnt, int _team_cnt, int _comp_win, int _comp_loose, int _comp_done)
		{
			char_id = _char_id;
			class_id = _class_id;
			char_name = _char_name;
			points_current = _points_current;
			points_past = _points_past;
			points_pre_past = _points_pre_past;
			class_free_cnt = _class_free_cnt;
			class_based_cnt = _class_based_cnt;
			team_cnt = _team_cnt;
			comp_win = _comp_win;
			comp_loose = _comp_loose;
			comp_done = _comp_done;
			rank = 0;
		}
	}
}