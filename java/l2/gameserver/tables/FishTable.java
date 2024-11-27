package l2.gameserver.tables;

import gnu.trove.TIntObjectHashMap;
import l2.commons.dbutils.DbUtils;
import l2.gameserver.database.DatabaseFactory;
import l2.gameserver.model.reward.RewardData;
import l2.gameserver.templates.FishTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class FishTable
{
	private static final Logger _log = LoggerFactory.getLogger(FishTable.class);
	private static final FishTable _instance = new FishTable();
	private TIntObjectHashMap<List<FishTemplate>> _fishes;
	private TIntObjectHashMap<List<RewardData>> _fishRewards;
	
	private FishTable()
	{
		load();
	}
	
	public static final FishTable getInstance()
	{
		return _instance;
	}
	
	public void reload()
	{
		load();
	}
	
	private void load()
	{
		_fishes = new TIntObjectHashMap();
		_fishRewards = new TIntObjectHashMap();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT id, level, name, hp, hpregen, fish_type, fish_group, fish_guts, guts_check_time, wait_time, combat_time FROM fish ORDER BY id");
			resultSet = statement.executeQuery();
			int count = 0;
			while(resultSet.next())
			{
				int id = resultSet.getInt("id");
				int lvl = resultSet.getInt("level");
				String name = resultSet.getString("name");
				int hp = resultSet.getInt("hp");
				int hpreg = resultSet.getInt("hpregen");
				int type = resultSet.getInt("fish_type");
				int group = resultSet.getInt("fish_group");
				int fish_guts = resultSet.getInt("fish_guts");
				int guts_check_time = resultSet.getInt("guts_check_time");
				int wait_time = resultSet.getInt("wait_time");
				int combat_time = resultSet.getInt("combat_time");
				FishTemplate fish = new FishTemplate(id, lvl, name, hp, hpreg, type, group, fish_guts, guts_check_time, wait_time, combat_time);
				ArrayList<FishTemplate> fishes = (ArrayList<FishTemplate>) _fishes.get(group);
				if(fishes == null)
				{
					fishes = new ArrayList<>();
					_fishes.put(group, fishes);
				}
				fishes.add(fish);
				++count;
			}
			DbUtils.close(statement, resultSet);
			_log.info("FishTable: Loaded " + count + " fishes.");
			count = 0;
			statement = con.prepareStatement("SELECT fishid, rewardid, min, max, chance FROM fishreward ORDER BY fishid");
			resultSet = statement.executeQuery();
			while(resultSet.next())
			{
				int fishid = resultSet.getInt("fishid");
				int rewardid = resultSet.getInt("rewardid");
				int mindrop = resultSet.getInt("min");
				int maxdrop = resultSet.getInt("max");
				int chance = resultSet.getInt("chance");
				RewardData reward = new RewardData(rewardid, mindrop, maxdrop, (double) chance * 10000.0);
				ArrayList<RewardData> rewards = (ArrayList<RewardData>) _fishRewards.get(fishid);
				if(rewards == null)
				{
					rewards = new ArrayList<>();
					_fishRewards.put(fishid, rewards);
				}
				rewards.add(reward);
				++count;
			}
			_log.info("FishTable: Loaded " + count + " fish rewards.");
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, resultSet);
		}
	}
	
	public int[] getFishIds()
	{
		return _fishRewards.keys();
	}
	
	public List<FishTemplate> getFish(int group, int type, int lvl)
	{
		List<FishTemplate> fishs = _fishes.get(group);
		if(fishs == null)
		{
			_log.warn("No fishes defined for group : " + group + "!");
			return null;
		}
		ArrayList<FishTemplate> result = new ArrayList<>();
		for(FishTemplate f : fishs)
		{
			if(f.getType() != type || f.getLevel() != lvl)
				continue;
			result.add(f);
		}
		if(result.isEmpty())
		{
			_log.warn("No fishes for group : " + group + " type: " + type + " level: " + lvl + "!");
		}
		return result;
	}
	
	public List<RewardData> getFishReward(int fishid)
	{
		List result = _fishRewards.get(fishid);
		if(_fishRewards == null)
		{
			_log.warn("No fish rewards defined for fish id: " + fishid + "!");
			return null;
		}
		if(result.isEmpty())
		{
			_log.warn("No fish rewards for fish id: " + fishid + "!");
		}
		return result;
	}
}