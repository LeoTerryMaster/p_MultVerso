package l2.gameserver.network.authcomm;

public enum ServerType
{
	NORMAL,
	RELAX,
	TEST,
	NO_LABEL,
	RESTRICTED,
	EVENT,
	FREE;
	
	private final int _mask;
	
	ServerType()
	{
		_mask = 1 << ordinal();
	}
	
	public int getMask()
	{
		return _mask;
	}
}