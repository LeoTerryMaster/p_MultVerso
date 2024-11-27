package l2.gameserver.model.base;

public enum AcquireType
{
	NORMAL,
	FISHING,
	CLAN;
	
	public static final AcquireType[] VALUES;
	
	static
	{
		VALUES = AcquireType.values();
	}
}