package achievements;

import l2.commons.lang.reference.HardReference;
import l2.commons.threading.RunnableImpl;
import l2.gameserver.ThreadPoolManager;
import l2.gameserver.model.Player;
import l2.gameserver.network.l2.components.CustomMessage;
import l2.gameserver.network.l2.s2c.ExShowScreenMessage;

public class Achievement
{
	public static final int BASE_ACHIEVEMENT_LEVEL = 1;
	private static final String VAR_NAME_FORMAT = "ex_achievement_%d_%s";
	private final AchievementInfo _achInfo;
	private final HardReference<Player> _playerRef;
	private final AchievementCounter _counter;
	private final long _expireTime;
	
	public Achievement(AchievementInfo achInfo, Player player)
	{
		_achInfo = achInfo;
		_playerRef = player.getRef();
		_counter = achInfo.getMetricType().getCounter(player, achInfo);
		_expireTime = Long.parseLong(getVar("expire_time", String.valueOf(_achInfo.nextResetTimeMills())));
		checkExpire();
	}
	
	private Player getPlayer()
	{
		return _playerRef.get();
	}
	
	public AchievementInfo getAchInfo()
	{
		return _achInfo;
	}
	
	private int getNextLevelNum()
	{
		return getVar("next_lvl", 1);
	}
	
	private void setNextLevelNum(int nextLevelNum)
	{
		setVar("next_lvl", nextLevelNum);
	}
	
	public void checkExpire()
	{
		long now = System.currentTimeMillis();
		if(_expireTime >= 0)
		{
			setVar("expire_time", String.valueOf(_expireTime));
		}
		if(_expireTime < 0 || _expireTime > now)
		{
			return;
		}
		_counter.setVal(0);
		_counter.store();
		setNextLevelNum(1);
		for(AchievementInfo.AchievementInfoLevel level : _achInfo.getLevels())
		{
			if(!isLevelRewarded(level))
				continue;
			setLevelRewarded(level, false);
		}
		long nextExpire = _achInfo.nextResetTimeMills();
		setVar("expire_time", String.valueOf(nextExpire));
	}
	
	private String getVar(String key, String defVal)
	{
		Player player = getPlayer();
		if(player == null)
		{
			return defVal;
		}
		String val = player.getVar(String.format("ex_achievement_%d_%s", _achInfo.getId(), key));
		return val != null ? val : defVal;
	}
	
	private int getVar(String key, int defVal)
	{
		return Integer.parseInt(getVar(key, String.valueOf(defVal)));
	}
	
	private void setVar(String key, String val)
	{
		Player player = getPlayer();
		if(player == null)
		{
			return;
		}
		player.setVar(String.format("ex_achievement_%d_%s", _achInfo.getId(), key), val, -1);
	}
	
	private void setVar(String key, int val)
	{
		setVar(key, String.valueOf(val));
	}
	
	public AchievementInfo.AchievementInfoLevel getLevel()
	{
		return _achInfo.getLevel(getNextLevelNum() - 1);
	}
	
	public boolean isRewardableLevel(AchievementInfo.AchievementInfoLevel level)
	{
		if(level == null || level.getAchievementInfo() != getAchInfo() || level.getLevel() >= getNextLevelNum() || level.getLevel() < 1)
		{
			return false;
		}
		return !isLevelRewarded(level);
	}
	
	public boolean isLevelRewarded(int level)
	{
		return getVar(String.format("lvl_%d_rewarded", level), 0) != 0;
	}
	
	public boolean isLevelRewarded(AchievementInfo.AchievementInfoLevel level)
	{
		return isLevelRewarded(level.getLevel());
	}
	
	public void setLevelRewarded(int level, boolean rewarded)
	{
		setVar(String.format("lvl_%d_rewarded", level), rewarded ? 1 : 0);
	}
	
	public void setLevelRewarded(AchievementInfo.AchievementInfoLevel level, boolean rewarded)
	{
		setLevelRewarded(level.getLevel(), rewarded);
	}
	
	public AchievementInfo.AchievementInfoLevel getNextLevel()
	{
		return _achInfo.getLevel(getNextLevelNum());
	}
	
	public boolean isCompleted()
	{
		int nextLevelNum = getNextLevelNum();
		return nextLevelNum > _achInfo.getMaxLevel();
	}
	
	public AchievementCounter getCounter()
	{
		return _counter;
	}
	
	public void onMetricEvent(Object... args)
	{
		Player player = getPlayer();
		if(player == null)
		{
			return;
		}
		if(!_achInfo.testConds(getPlayer(), args))
		{
			return;
		}
		if(_achInfo.getMetricNotifyDelay() > 0)
		{
			ThreadPoolManager.getInstance().schedule(new EventMetric(player, args), _achInfo.getMetricNotifyDelay() * 1000);
		}
		else
		{
			ThreadPoolManager.getInstance().execute(new EventMetric(player, args));
		}
	}
	
	private class EventMetric extends RunnableImpl
	{
		private final Object[] _args;
		private final HardReference<Player> _playerRef;
		
		private EventMetric(Player player, Object[] args)
		{
			_args = args;
			_playerRef = player.getRef();
		}
		
		@Override
		public void runImpl() throws Exception
		{
			Player player = _playerRef.get();
			if(player == null)
			{
				return;
			}
			AchievementInfo.AchievementInfoLevel nextLevelInfo = getNextLevel();
			if(nextLevelInfo != null && nextLevelInfo.testConds(player, _args))
			{
				AchievementCounter counter = getCounter();
				int value = counter.incrementAndGetValue();
				if(value >= nextLevelInfo.getValue())
				{
					AchievementInfo.AchievementInfoLevel currLevelInfo = nextLevelInfo;
					setNextLevelNum(currLevelInfo.getLevel() + 1);
					if(currLevelInfo.isResetMetric())
					{
						counter.setVal(0);
					}
					String screenText = new CustomMessage("achievements.achievementS1Unlocked", player, new Object[] {_achInfo.getName(player)}).toString();
					player.sendPacket(new ExShowScreenMessage(screenText, 5000, 0, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false, 1, -1, true));
					player.sendMessage(new CustomMessage("achievements.achievedS1LevelS2", player, _achInfo.getName(player), nextLevelInfo.getLevel()));
				}
				counter.store();
			}
		}
	}
}