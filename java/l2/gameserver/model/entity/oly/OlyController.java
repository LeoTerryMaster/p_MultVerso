package l2.gameserver.model.entity.oly;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.Announcements;
import l2.gameserver.Config;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.cache.Msg;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.instancemanager.ServerVariables;
import l2.gameserver.model.GameObjectsStorage;
import l2.gameserver.model.instances.NpcInstance;
import l2.gameserver.network.l2.s2c.SystemMessage;
import l2.gameserver.scripts.Functions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class OlyController
{
	private static final Logger _log = LoggerFactory.getLogger(OlyController.class);
	private static final SimpleDateFormat _dtformat = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
	private static final String VAR_SEASON_ID = "oly_season_id";
	private static final String VAR_SEASON_CALC = "oly_season_calc";
	private static final String SQL_LOAD_SEASON_TIME = "SELECT `season_id`,`season_start_time`,`season_end_time`,`nominate_start`,`b_idx`,`b_s0`,`b_s1`,`b_s2`,`b_s3`,`c_idx`,`c_s0`,`c_e0`,`c_s1`,`c_e1`,`c_s2`,`c_e2`,`c_s3`,`c_e3`,`c_s4`,`c_e4`,`c_s5`,`c_e5`,`c_s6`,`c_e6`,`c_s7`,`c_e7`,`c_s8`,`c_e8`,`c_s9`,`c_e9`,`c_s10`,`c_e10`,`c_s11`,`c_e11`,`c_s12`,`c_e12`,`c_s13`,`c_e13`,`c_s14`,`c_e14`,`c_s15`,`c_e15`,`c_s16`,`c_e16`,`c_s17`,`c_e17`,`c_s18`,`c_e18`,`c_s19`,`c_e19`,`c_s20`,`c_e20`,`c_s21`,`c_e21`,`c_s22`,`c_e22`,`c_s23`,`c_e23`,`c_s24`,`c_e24`,`c_s25`,`c_e25`,`c_s26`,`c_e26`,`c_s27`,`c_e27`,`c_s28`,`c_e28`,`c_s29`,`c_e29`,`c_s30`,`c_e30` FROM `oly_season` WHERE `season_id` = ?";
	private static final String SQL_SAVE_SEASON_TIME = "REPLACE INTO `oly_season`(`season_id`,`season_start_time`,`season_end_time`,`nominate_start`,`b_idx`,`b_s0`,`b_s1`,`b_s2`,`b_s3`,`c_idx`,`c_s0`,`c_e0`,`c_s1`,`c_e1`,`c_s2`,`c_e2`,`c_s3`,`c_e3`,`c_s4`,`c_e4`,`c_s5`,`c_e5`,`c_s6`,`c_e6`,`c_s7`,`c_e7`,`c_s8`,`c_e8`,`c_s9`,`c_e9`,`c_s10`,`c_e10`,`c_s11`,`c_e11`,`c_s12`,`c_e12`,`c_s13`,`c_e13`,`c_s14`,`c_e14`,`c_s15`,`c_e15`,`c_s16`,`c_e16`,`c_s17`,`c_e17`,`c_s18`,`c_e18`,`c_s19`,`c_e19`,`c_s20`,`c_e20`,`c_s21`,`c_e21`,`c_s22`,`c_e22`,`c_s23`,`c_e23`,`c_s24`,`c_e24`,`c_s25`,`c_e25`,`c_s26`,`c_e26`,`c_s27`,`c_e27`,`c_s28`,`c_e28`,`c_s29`,`c_e29`,`c_s30`,`c_e30`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String PART_CNT_VAR = "@OlyPartCnt";
	private static final String OLY_HERO_SEASON_VAR = "oly_chero_season";
	private static OlyController _instance;
	
	static
	{
		_dtformat.setTimeZone(TimeZone.getDefault());
		_instance = null;
	}
	
	private final ScheduledFuture<?>[][] _comps_start_tasks;
	private final ScheduledFuture<?>[] _bonus_tasks;
	private final long[] _bonus_time = new long[4];
	private final long[][] _comps_time = new long[31][];
	private int _season_id;
	private boolean _season_calculation;
	private boolean _is_comp_active;
	private ScheduledFuture<?> _season_start_task;
	private ScheduledFuture<?> _season_end_task;
	private ScheduledFuture<?> _nominate_task;
	private long _season_start_time;
	private long _season_end_time;
	private long _nominate_time;
	private int _bonus_idx;
	private int _comp_idx;
	private int _part_count;
	private int _active_comp_idx = -1;
	
	private OlyController()
	{
		_comps_start_tasks = new ScheduledFuture[_comps_time.length][];
		_bonus_tasks = new ScheduledFuture[_bonus_time.length];
		load();
		schedule();
	}
	
	public static final OlyController getInstance()
	{
		if(_instance == null)
		{
			_instance = new OlyController();
		}
		return _instance;
	}
	
	private static String ScheduledFutureTime(ScheduledFuture<?> future)
	{
		return UnixTimeStampToString(System.currentTimeMillis() / 1000 + future.getDelay(TimeUnit.SECONDS));
	}
	
	private static String UnixTimeStampToString(long dt)
	{
		return _dtformat.format(new Date((dt + 1) * 1000));
	}
	
	public synchronized void load()
	{
		_season_id = ServerVariables.getInt("oly_season_id", 0);
		_season_calculation = ServerVariables.getBool("oly_season_calc", false);
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			pstmt = conn.prepareStatement("SELECT `season_id`,`season_start_time`,`season_end_time`,`nominate_start`,`b_idx`,`b_s0`,`b_s1`,`b_s2`,`b_s3`,`c_idx`,`c_s0`,`c_e0`,`c_s1`,`c_e1`,`c_s2`,`c_e2`,`c_s3`,`c_e3`,`c_s4`,`c_e4`,`c_s5`,`c_e5`,`c_s6`,`c_e6`,`c_s7`,`c_e7`,`c_s8`,`c_e8`,`c_s9`,`c_e9`,`c_s10`,`c_e10`,`c_s11`,`c_e11`,`c_s12`,`c_e12`,`c_s13`,`c_e13`,`c_s14`,`c_e14`,`c_s15`,`c_e15`,`c_s16`,`c_e16`,`c_s17`,`c_e17`,`c_s18`,`c_e18`,`c_s19`,`c_e19`,`c_s20`,`c_e20`,`c_s21`,`c_e21`,`c_s22`,`c_e22`,`c_s23`,`c_e23`,`c_s24`,`c_e24`,`c_s25`,`c_e25`,`c_s26`,`c_e26`,`c_s27`,`c_e27`,`c_s28`,`c_e28`,`c_s29`,`c_e29`,`c_s30`,`c_e30` FROM `oly_season` WHERE `season_id` = ?");
			pstmt.setInt(1, _season_id);
			rset = pstmt.executeQuery();
			if(rset.next())
			{
				_season_start_time = rset.getLong("season_start_time");
				_season_end_time = rset.getLong("season_end_time");
				_nominate_time = rset.getLong("nominate_start");
				_bonus_idx = rset.getInt("b_idx");
				_bonus_time[0] = rset.getLong("b_s0");
				_bonus_time[1] = rset.getLong("b_s1");
				_bonus_time[2] = rset.getLong("b_s2");
				_bonus_time[3] = rset.getLong("b_s3");
				_comp_idx = rset.getInt("c_idx");
				for(int i = 0;i < _comps_time.length;++i)
				{
					long[] comp_time = {rset.getLong("c_s" + i), rset.getLong("c_e" + i)};
					_comps_time[i] = comp_time;
				}
			}
			else
			{
				_log.info("Oly: Generating a new season " + _season_id);
				calcNewSeason();
				save();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(conn, pstmt, rset);
		}
		_part_count = ServerVariables.getInt("@OlyPartCnt", 0);
	}
	
	public synchronized void save()
	{
		Connection conn = null;
		PreparedStatement pstmt = null;
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			pstmt = conn.prepareStatement("REPLACE INTO `oly_season`(`season_id`,`season_start_time`,`season_end_time`,`nominate_start`,`b_idx`,`b_s0`,`b_s1`,`b_s2`,`b_s3`,`c_idx`,`c_s0`,`c_e0`,`c_s1`,`c_e1`,`c_s2`,`c_e2`,`c_s3`,`c_e3`,`c_s4`,`c_e4`,`c_s5`,`c_e5`,`c_s6`,`c_e6`,`c_s7`,`c_e7`,`c_s8`,`c_e8`,`c_s9`,`c_e9`,`c_s10`,`c_e10`,`c_s11`,`c_e11`,`c_s12`,`c_e12`,`c_s13`,`c_e13`,`c_s14`,`c_e14`,`c_s15`,`c_e15`,`c_s16`,`c_e16`,`c_s17`,`c_e17`,`c_s18`,`c_e18`,`c_s19`,`c_e19`,`c_s20`,`c_e20`,`c_s21`,`c_e21`,`c_s22`,`c_e22`,`c_s23`,`c_e23`,`c_s24`,`c_e24`,`c_s25`,`c_e25`,`c_s26`,`c_e26`,`c_s27`,`c_e27`,`c_s28`,`c_e28`,`c_s29`,`c_e29`,`c_s30`,`c_e30`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			pstmt.setInt(1, _season_id);
			pstmt.setLong(2, _season_start_time);
			pstmt.setLong(3, _season_end_time);
			pstmt.setLong(4, _nominate_time);
			pstmt.setInt(5, _bonus_idx);
			for(int i = 0;i < _bonus_time.length;++i)
			{
				pstmt.setLong(6 + i, _bonus_time[i]);
			}
			pstmt.setInt(10, _comp_idx);
			for(int j = 0;j < _comps_time.length;++j)
			{
				pstmt.setLong(11 + j * 2, _comps_time[j][0]);
				pstmt.setLong(12 + j * 2, _comps_time[j][1]);
			}
			pstmt.executeUpdate();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(conn, pstmt);
		}
		ServerVariables.set("oly_season_id", _season_id);
		ServerVariables.set("oly_season_calc", _season_calculation);
		ServerVariables.set("@OlyPartCnt", _part_count);
	}
	
	private void schedule()
	{
		long now = System.currentTimeMillis() / 1000;
		int curr_season = _season_id;
		_season_start_task = ThreadPoolManager.getInstance().schedule(new SeasonStartTask(curr_season), Math.max(60, _season_start_time - now) * 1000);
		_log.info("OlyController: Season " + curr_season + " start schedule at " + ScheduledFutureTime(_season_start_task));
		_season_end_task = ThreadPoolManager.getInstance().schedule(new SeasonEndTask(curr_season), Math.max(240, _season_end_time - now) * 1000);
		_log.info("OlyController: Season " + curr_season + " end schedule at " + ScheduledFutureTime(_season_end_task));
		_nominate_task = ThreadPoolManager.getInstance().schedule(new NominationTask(curr_season), Math.max(600, _nominate_time - now) * 1000);
		_log.info("OlyController: Season " + curr_season + " nomination schedule at " + ScheduledFutureTime(_nominate_task));
		StringBuilder sb = new StringBuilder();
		for(int i = _comp_idx;i < _comps_time.length;++i)
		{
			if(_comps_time[i] == null || _comps_time[i][0] < now && _comps_time[i][1] < now)
				continue;
			if(i != _comp_idx)
			{
				sb.append(';');
			}
			_comps_start_tasks[i] = new ScheduledFuture[] {ThreadPoolManager.getInstance().schedule(new CompetitionStartTask(curr_season, i), Math.max(60, _comps_time[i][0] - now) * 1000), ThreadPoolManager.getInstance().schedule(new CompetitionEndTask(curr_season, i), Math.max(60, _comps_time[i][1] - now) * 1000)};
			sb.append(ScheduledFutureTime(_comps_start_tasks[i][0]) + "-" + ScheduledFutureTime(_comps_start_tasks[i][1]));
		}
		_log.info("OlyController: Season " + curr_season + " competitions schedule at [" + sb + "]");
		sb.delete(0, sb.length());
		for(int j = _bonus_idx;j < _bonus_time.length;++j)
		{
			if(_bonus_time[j] <= now)
				continue;
			if(j != _bonus_idx)
			{
				sb.append(';');
			}
			_bonus_tasks[j] = ThreadPoolManager.getInstance().schedule(new BonusTask(curr_season, j), Math.max((long) ((3 + j - _bonus_idx) * 60), _bonus_time[j] - now) * 1000);
			sb.append(ScheduledFutureTime(_bonus_tasks[j]));
		}
		_log.info("OlyController: Season " + curr_season + " bonuses schedule at [" + sb + "]");
	}
	
	private synchronized void SeasonStart(int season_id)
	{
		try
		{
			_season_calculation = false;
			Announcements.getInstance().announceToAll(new SystemMessage(1639).addNumber(season_id));
			_log.info("OlyController: Season " + season_id + " started.");
		}
		catch(Exception e)
		{
			_log.warn("Exception while starting of " + season_id + " season", e);
		}
	}
	
	private synchronized void SeasonEnd(int season_id)
	{
		try
		{
			if(!_season_calculation)
			{
				_season_calculation = true;
				if(ServerVariables.getInt("oly_chero_season", -1) != season_id)
				{
					_log.info("OlyController: calculation heroes for " + season_id + " season");
					HeroController.getInstance().ComputeNewHeroNobleses();
					ServerVariables.set("oly_chero_season", season_id);
				}
				save();
				Announcements.getInstance().announceToAll(new SystemMessage(1640).addNumber(season_id));
			}
			else
			{
				_log.warn("OlyController: Unexpected season calculated. Canceling computation.");
			}
			_log.info("OlyController: Season " + season_id + " ended.");
		}
		catch(Exception e)
		{
			_log.warn("Exception while ending of " + season_id + " season", e);
		}
	}
	
	private synchronized void Nomination(int season_id)
	{
		try
		{
			if(_season_calculation)
			{
				_season_calculation = false;
				save();
			}
			else
			{
				_log.warn("OlyController: Season not calculated. Run calculation manualy.");
			}
			_log.info("OlyController: Season " + season_id + " nomination started.");
			ThreadPoolManager.getInstance().execute(new NewSeasonCalcTask(season_id + 1));
		}
		catch(Exception e)
		{
			_log.warn("Exception while nominating in " + season_id + " season", e);
		}
	}
	
	private synchronized void CompetitionStart(int season_id, int comp_id)
	{
		try
		{
			if(!_is_comp_active)
			{
				_is_comp_active = true;
				StadiumPool.getInstance().AllocateStadiums();
				ParticipantPool.getInstance().AllocatePools();
				CompetitionController.getInstance();
				CompetitionController.getInstance().scheduleStartTask();
				Announcements.getInstance().announceToAll(Msg.THE_OLYMPIAD_GAME_HAS_STARTED);
				_log.info("OlyController: Season " + season_id + " comp " + comp_id + " started.");
				_active_comp_idx = comp_id;
			}
			else
			{
				_log.warn("OlyController: Can't start new competitions. Old comps in progress.");
			}
		}
		catch(Exception e)
		{
			_log.warn("Exception while start comp " + comp_id + " in " + season_id + " season", e);
		}
	}
	
	private synchronized void CompetitionEnd(int season_id, int comp_id)
	{
		try
		{
			if(_is_comp_active)
			{
				CompetitionController.getInstance().cancelStartTask();
				_is_comp_active = false;
				StadiumPool.getInstance().FreeStadiums();
				ParticipantPool.getInstance().FreePools();
				_active_comp_idx = -1;
				++_comp_idx;
				Announcements.getInstance().announceToAll(new SystemMessage(1919));
				Announcements.getInstance().announceToAll(Msg.THE_OLYMPIAD_GAME_HAS_ENDED);
				_log.info("OlyController: Season " + season_id + " comp " + comp_id + " ended.");
				save();
			}
			else
			{
				_log.warn("OlyController: Can't stop competitions. Competitions not in progress.");
			}
		}
		catch(Exception e)
		{
			_log.warn("Exception while end comp " + comp_id + " in " + season_id + " season", e);
		}
	}
	
	private synchronized void Bonus(int season_id, int bonus_id)
	{
		try
		{
			NoblesController.getInstance().AddWeaklyBonus();
			++_bonus_idx;
			_log.info("OlyController: Season " + season_id + " bonus " + bonus_id + " applied.");
			save();
		}
		catch(Exception e)
		{
			_log.warn("Exception while bonus " + bonus_id + " in " + season_id + " season", e);
		}
	}
	
	private synchronized void NewSeasonCalc(int season_id)
	{
		try
		{
			save();
			_season_id = season_id;
			if(Config.OLY_RECALC_NEW_SEASON)
			{
				calcNewSeason();
				save();
			}
			else
			{
				ServerVariables.set("oly_season_id", _season_id);
				load();
			}
			schedule();
		}
		catch(Exception e)
		{
			_log.warn("Exception while calculating new " + season_id + " season", e);
		}
	}
	
	public boolean isCompetitionsActive()
	{
		return _is_comp_active;
	}
	
	public boolean isRegAllowed()
	{
		if(_is_comp_active && _active_comp_idx >= 0)
		{
			return System.currentTimeMillis() < (_comps_time[_active_comp_idx][1] - 300) * 1000;
		}
		return _is_comp_active;
	}
	
	public boolean isCalculationPeriod()
	{
		return _season_calculation;
	}
	
	public int getCurrentSeason()
	{
		return _season_id;
	}
	
	public int getCurrentPeriod()
	{
		return _bonus_idx;
	}
	
	public void shutdown()
	{
		if(_is_comp_active)
		{
			CompetitionController.getInstance().cancelStartTask();
			StadiumPool.getInstance().FreeStadiums();
			ParticipantPool.getInstance().FreePools();
		}
	}
	
	public void announceCompetition(CompetitionType type, int stad_id)
	{
		for(NpcInstance npc : GameObjectsStorage.getAllByNpcId(31688, false))
		{
			if(!Config.NPC_OLYMPIAD_GAME_ANNOUNCE)
				continue;
            switch (type) {
                case CLASS_FREE -> {
                    Functions.npcShoutCustomMessage(npc, "l2p.gameserver.model.entity.OlympiadGame.OlympiadNonClassed", String.valueOf(stad_id + 1));
                }
                case CLASS_INDIVIDUAL -> {
                    Functions.npcShoutCustomMessage(npc, "l2p.gameserver.model.entity.OlympiadGame.OlympiadClassed", String.valueOf(stad_id + 1));
                }
                case TEAM_CLASS_FREE -> {
                    Functions.npcShoutCustomMessage(npc, "l2p.gameserver.model.entity.OlympiadGame.OlympiadTeam", String.valueOf(stad_id + 1));
                }
            }
		}
	}
	
	public int getPartCount()
	{
		return _part_count;
	}
	
	public int getCurrentPartCount()
	{
		return CompetitionController.getInstance().getCompetitions().size();
	}
	
	public void incPartCount()
	{
		++_part_count;
	}
	
	private final synchronized void calcNewSeason()
	{
		Calendar base = Calendar.getInstance();
		if(Config.OLY_SEASON_TIME_CALC_MODE == Config.OlySeasonTimeCalcMode.NORMAL)
		{
			base.set(Calendar.DATE, 1);
		}
		else
		{
			base.set(Calendar.DATE, base.get(Calendar.DATE));
		}
		base.set(Calendar.HOUR_OF_DAY, 0);
		base.set(Calendar.MINUTE, 0);
		base.set(Calendar.SECOND, 0);
		base.set(Calendar.MILLISECOND, 0);
		long base_mills = base.getTimeInMillis();
		_season_start_time = getDateSeconds(base_mills, Config.OLY_SEASON_START_TIME);
		_season_end_time = getDateSeconds(base_mills, Config.OLY_SEASON_END_TIME);
		_nominate_time = getDateSeconds(base_mills, Config.OLY_NOMINATE_TIME);
		base_mills = _season_start_time * 1000;
		Calendar c_bonus = Calendar.getInstance();
		c_bonus.setTimeInMillis(base_mills);
		for(int i = 0;i < _bonus_time.length;++i)
		{
			_bonus_time[i] = getDateSeconds(c_bonus, Config.OLY_BONUS_TIME);
		}
		_bonus_idx = 0;
		Calendar c_comp_start = Calendar.getInstance();
		c_comp_start.setTimeInMillis(base_mills);
		for(int j = 0;j < _comps_time.length;++j)
		{
			_comps_time[j] = new long[2];
			_comps_time[j][0] = getDateSeconds(c_comp_start, Config.OLY_COMPETITION_START_TIME);
			_comps_time[j][1] = getDateSeconds(c_comp_start, Config.OLY_COMPETITION_END_TIME);
			if(_comps_time[j][0] > _season_end_time)
			{
				_comps_time[j][0] = -1;
				_comps_time[j][1] = -1;
			}
			if(_comps_time[j][1] < _season_end_time - 300)
				continue;
			long[] arrl = _comps_time[j];
			arrl[1] = arrl[1] - 300;
		}
		_comp_idx = 0;
	}
	
	private long getDateSeconds(long mills, String rule)
	{
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(mills));
		return getDateSeconds(c, rule);
	}
	
	private long getDateSeconds(Calendar c, String rule)
	{
		String[] parts = rule.split("\\s+");
		if(parts.length == 2)
		{
			String datepartsstr = parts[0];
			String[] dateparts = datepartsstr.split(":");
			if(dateparts.length == 2)
			{
				if(dateparts[0].startsWith("+"))
				{
					c.add(2, Integer.parseInt(dateparts[0].substring(1)));
				}
				else
				{
					c.set(2, Integer.parseInt(dateparts[0]) - 1);
				}
			}
			String datemodstr;
			if((datemodstr = dateparts[dateparts.length - 1]).startsWith("+"))
			{
				c.add(5, Integer.parseInt(datemodstr.substring(1)));
			}
			else
			{
				c.set(5, Integer.parseInt(datemodstr));
			}
		}
		String[] timeparts;
		if((timeparts = parts[parts.length - 1].split(":"))[0].startsWith("+"))
		{
			c.add(11, Integer.parseInt(timeparts[0].substring(1)));
		}
		else
		{
			c.set(11, Integer.parseInt(timeparts[0]));
		}
		if(timeparts[1].startsWith("+"))
		{
			c.add(12, Integer.parseInt(timeparts[1].substring(1)));
		}
		else
		{
			c.set(12, Integer.parseInt(timeparts[1]));
		}
		return c.getTimeInMillis() / 1000;
	}
	
	public long getSeasonStartTime()
	{
		return _season_start_time;
	}
	
	public long getSeasonEndTime()
	{
		return _season_end_time;
	}
	
	private class NewSeasonCalcTask implements Runnable
	{
		private final int season_id;
		
		public NewSeasonCalcTask(int season_id_)
		{
			season_id = season_id_;
		}
		
		@Override
		public void run()
		{
			getInstance().NewSeasonCalc(season_id);
		}
	}
	
	private class BonusTask implements Runnable
	{
		private final int season_id;
		private final int bonus_id;
		
		public BonusTask(int season_id_, int bonus_id_)
		{
			season_id = season_id_;
			bonus_id = bonus_id_;
		}
		
		@Override
		public void run()
		{
			getInstance().Bonus(season_id, bonus_id);
		}
	}
	
	private class CompetitionEndTask implements Runnable
	{
		private final int season_id;
		private final int comp_id;
		
		public CompetitionEndTask(int season_id_, int comp_id_)
		{
			season_id = season_id_;
			comp_id = comp_id_;
		}
		
		@Override
		public void run()
		{
			getInstance().CompetitionEnd(season_id, comp_id);
		}
	}
	
	private class CompetitionStartTask implements Runnable
	{
		private final int season_id;
		private final int comp_id;
		
		public CompetitionStartTask(int season_id_, int comp_id_)
		{
			season_id = season_id_;
			comp_id = comp_id_;
		}
		
		@Override
		public void run()
		{
			getInstance().CompetitionStart(season_id, comp_id);
		}
	}
	
	private class NominationTask implements Runnable
	{
		private final int season_id;
		
		public NominationTask(int season_id_)
		{
			season_id = season_id_;
		}
		
		@Override
		public void run()
		{
			getInstance().Nomination(season_id);
		}
	}
	
	private class SeasonEndTask implements Runnable
	{
		private final int season_id;
		
		public SeasonEndTask(int season_id_)
		{
			season_id = season_id_;
		}
		
		@Override
		public void run()
		{
			getInstance().SeasonEnd(season_id);
		}
	}
	
	private class SeasonStartTask implements Runnable
	{
		private final int season_id;
		
		public SeasonStartTask(int season_id_)
		{
			season_id = season_id_;
		}
		
		@Override
		public void run()
		{
			getInstance().SeasonStart(season_id);
		}
	}
}