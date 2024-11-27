package l2.gameserver.model.reward;

public enum RewardType
{
	RATED_GROUPED,
	NOT_RATED_NOT_GROUPED,
	NOT_RATED_GROUPED,
	SWEEP;
	
	public static final RewardType[] VALUES;
	
	static
	{
		VALUES = RewardType.values();
	}
}