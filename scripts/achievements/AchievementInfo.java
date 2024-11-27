package achievements;

import l2.commons.time.cron.NextTime;
import l2.gameserver.data.StringHolder;
import l2.gameserver.model.Player;
import l2.gameserver.model.reward.RewardData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AchievementInfo
{
	private final int _id;
	private final AchievementMetricType _metricType;
	private final long _metricNotifyDelay;
	private final String _nameAddr;
	private final NextTime _expireTime;
	private final Map<Integer, AchievementInfoLevel> _levelsMap = new TreeMap<>();
	private final List<AchievementInfoLevel> _levels = new ArrayList<>();
	private AchievementInfoCategory _category;
	private int _minLevel = Integer.MAX_VALUE;
	private int _maxLevel = Integer.MIN_VALUE;
	private List<AchievementCondition> _condList = new ArrayList<>();
	private String _icon = "icon.etc_plan_i00";
	
	public AchievementInfo(int id, AchievementMetricType metricType, long metricNotifyDelay, String nameAddr, NextTime expireTime)
	{
		_id = id;
		_metricType = metricType;
		_metricNotifyDelay = metricNotifyDelay;
		_nameAddr = nameAddr;
		_expireTime = expireTime;
		_maxLevel = 0;
	}
	
	public String getIcon()
	{
		return _icon;
	}
	
	public void setIcon(String icon)
	{
		_icon = icon;
	}
	
	public AchievementInfoCategory getCategory()
	{
		return _category;
	}
	
	public void setCategory(AchievementInfoCategory category)
	{
		_category = category;
	}
	
	public long nextResetTimeMills()
	{
		if(_expireTime == null)
		{
			return -1;
		}
		return _expireTime.next(System.currentTimeMillis());
	}
	
	public int getId()
	{
		return _id;
	}
	
	public void addCond(AchievementCondition cond)
	{
		_condList.add(cond);
	}
	
	public void addLevel(AchievementInfoLevel achInfoLevel)
	{
		if(achInfoLevel.getLevel() > _maxLevel)
		{
			_maxLevel = achInfoLevel.getLevel();
		}
		if(achInfoLevel.getLevel() < _minLevel)
		{
			_minLevel = achInfoLevel.getLevel();
		}
		_levelsMap.put(achInfoLevel.getLevel(), achInfoLevel);
		_levels.add(achInfoLevel);
		Collections.sort(_levels);
	}
	
	public AchievementInfoLevel addLevel(int level, int value, String descAddr, boolean resetMetric)
	{
		AchievementInfoLevel achievementInfoLevel = new AchievementInfoLevel(this, level, value, descAddr, resetMetric);
		addLevel(achievementInfoLevel);
		return achievementInfoLevel;
	}
	
	public int getMaxLevel()
	{
		return _maxLevel;
	}
	
	public List<AchievementCondition> getCondList()
	{
		return _condList;
	}
	
	public void setCondList(List<AchievementCondition> condList)
	{
		_condList = condList;
	}
	
	public AchievementMetricType getMetricType()
	{
		return _metricType;
	}
	
	public long getMetricNotifyDelay()
	{
		return _metricNotifyDelay;
	}
	
	public List<AchievementInfoLevel> getLevels()
	{
		return _levels;
	}
	
	public AchievementInfoLevel getLevel(int lvl)
	{
		return _levelsMap.get(lvl);
	}
	
	public String getNameAddr()
	{
		return _nameAddr;
	}
	
	public String getName(Player player)
	{
		return StringHolder.getInstance().getNotNull(player, getNameAddr());
	}
	
	public boolean testConds(Player player, Object... args)
	{
		for(AchievementCondition cond : getCondList())
		{
			if(cond.test(player, args))
				continue;
			return false;
		}
		return true;
	}
	
	public static class AchievementInfoLevel implements Comparable<AchievementInfoLevel>
	{
		private final AchievementInfo _achievementInfo;
		private final int _level;
		private final int _value;
		private final String _descAddr;
		private final boolean _resetMetric;
		private final List<RewardData> _rewardDataList = new ArrayList<>();
		private final List<AchievementCondition> _achievementConditions = new ArrayList<>();
		
		public AchievementInfoLevel(AchievementInfo info, int level, int value, String descAddr, boolean resetMetric)
		{
			_achievementInfo = info;
			_level = level;
			_value = value;
			_descAddr = descAddr;
			_resetMetric = resetMetric;
		}
		
		public AchievementInfo getAchievementInfo()
		{
			return _achievementInfo;
		}
		
		public boolean isResetMetric()
		{
			return _resetMetric;
		}
		
		public int getLevel()
		{
			return _level;
		}
		
		public int getValue()
		{
			return _value;
		}
		
		public String getDescAddr()
		{
			return _descAddr;
		}
		
		public List<RewardData> getRewardDataList()
		{
			return _rewardDataList;
		}
		
		public List<AchievementCondition> getCondList()
		{
			return _achievementConditions;
		}
		
		public void addCond(AchievementCondition achievementCondition)
		{
			_achievementConditions.add(achievementCondition);
		}
		
		public void addRewardData(RewardData rewardData)
		{
			_rewardDataList.add(rewardData);
		}
		
		public String getDesc(Player player)
		{
			return StringHolder.getInstance().getNotNull(player, getDescAddr());
		}
		
		public boolean testConds(Player player, Object... args)
		{
			for(AchievementCondition cond : getCondList())
			{
				if(cond.test(player, args))
					continue;
				return false;
			}
			return true;
		}
		
		@Override
		public int compareTo(AchievementInfoLevel other)
		{
			if(getAchievementInfo().getId() != other.getAchievementInfo().getId())
			{
				return Integer.compare(getAchievementInfo().getId(), other.getAchievementInfo().getId());
			}
			return Integer.compare(getLevel(), other.getLevel());
		}
	}
	
	public static class AchievementInfoCategory
	{
		private final String _name;
		private final String _titleAddress;
		
		public AchievementInfoCategory(String name, String titleAddress)
		{
			_name = name;
			_titleAddress = titleAddress;
		}
		
		public String getName()
		{
			return _name;
		}
		
		public String getTitle(Player player)
		{
			return StringHolder.getInstance().getNotNull(player, _titleAddress);
		}
	}
}