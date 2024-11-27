package achievements;

import l2.gameserver.model.Player;

public enum AchievementMetricType
{
	LOGIN,
	NPC_KILL,
	PVP_KILL,
	DEATH,
	LEVEL
			{
				@Override
				public AchievementCounter getCounter(Player player, AchievementInfo achInfo)
				{
					int playerLevel = player.getLevel();
					return new AchievementCounter(player.getObjectId(), achInfo.getId())
					{
						
						@Override
						public int getVal()
						{
							return playerLevel;
						}
						
						@Override
						public void setVal(int val)
						{
						}
						
						@Override
						public int incrementAndGetValue()
						{
							return playerLevel;
						}
						
						@Override
						public void store()
						{
						}
						
						@Override
						public boolean isStorable()
						{
							return false;
						}
					};
				}
			},
	OLYMPIAD,
	RAID_PARTICIPATION,
	QUEST_STATE;
	
	
	public AchievementCounter getCounter(Player player, AchievementInfo achInfo)
	{
		return AchievementCounter.makeDBStorableCounter(player.getObjectId(), achInfo.getId());
	}
}