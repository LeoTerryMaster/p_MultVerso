package l2.gameserver.tables;

import l2.commons.dbutils.DbUtils;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.Creature;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LevelUpTable
{
	private static final Logger _log = Logger.getLogger(LevelUpTable.class.getName());
	private static LevelUpTable _instance;
	private int _maxLvl;
	private int _maxClassID;
	private double[] _hp_table;
	private double[] _cp_table;
	private double[] _mp_table;
	
	private LevelUpTable()
	{
		loadData();
	}
	
	public static final LevelUpTable getInstance()
	{
		if(_instance == null)
		{
			_instance = new LevelUpTable();
		}
		return _instance;
	}
	
	private void loadData()
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rset = null;
		try
		{
			conn = DatabaseFactory.getInstance().getConnection();
			stmt = conn.createStatement();
			rset = stmt.executeQuery("SELECT MAX(`lvl`) FROM `lvlupgain`");
			if(rset.next())
			{
				_maxLvl = rset.getInt(1);
			}
			DbUtils.closeQuietly(stmt, rset);
			stmt = conn.createStatement();
			rset = stmt.executeQuery("SELECT MAX(`class_id`) FROM `lvlupgain`");
			if(rset.next())
			{
				_maxClassID = rset.getInt(1);
			}
			DbUtils.closeQuietly(stmt, rset);
			int max_idx = getIdx(_maxLvl, _maxClassID) + 1;
			_hp_table = new double[max_idx];
			_cp_table = new double[max_idx];
			_mp_table = new double[max_idx];
			stmt = conn.createStatement();
			rset = stmt.executeQuery("SELECT `class_id`,`lvl`,`hp`,`cp`,`mp` FROM `lvlupgain`");
			while(rset.next())
			{
				int idx = getIdx(rset.getInt("lvl"), rset.getInt("class_id"));
				_hp_table[idx] = rset.getDouble("hp");
				_cp_table[idx] = rset.getDouble("cp");
				_mp_table[idx] = rset.getDouble("mp");
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.SEVERE, "Can't load lvlupgain table ", e);
		}
		finally
		{
			DbUtils.closeQuietly(conn, stmt, rset);
		}
	}
	
	public double getMaxHP(Creature character)
	{
		if(character.isPlayer())
		{
			return _hp_table[getIdx(character.getLevel(), character.getPlayer().getClassId().getId())];
		}
		return character.getTemplate().baseHpMax;
	}
	
	public double getMaxCP(Creature character)
	{
		if(character.isPlayer())
		{
			return _cp_table[getIdx(character.getLevel(), character.getPlayer().getClassId().getId())];
		}
		return character.getTemplate().baseCpMax;
	}
	
	public double getMaxMP(Creature character)
	{
		if(character.isPlayer())
		{
			return _mp_table[getIdx(character.getLevel(), character.getPlayer().getClassId().getId())];
		}
		return character.getTemplate().baseMpMax;
	}
	
	private int getIdx(int lvl, int class_id)
	{
		return lvl << 8 | class_id & 255;
	}
}