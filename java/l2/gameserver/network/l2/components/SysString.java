package l2.gameserver.network.l2.components;

public enum SysString
{
	PASSENGER_BOAT_INFO(801),
	PREVIOUS(1037),
	NEXT(1038);
	
	private static final SysString[] VALUES;
	
	static
	{
		VALUES = SysString.values();
	}
	
	private final int _id;
	
	SysString(int i)
	{
		_id = i;
	}
	
	public static SysString valueOf2(String id)
	{
		for(SysString m : VALUES)
		{
			if(!m.name().equals(id))
				continue;
			return m;
		}
		return null;
	}
	
	public static SysString valueOf(int id)
	{
		for(SysString m : VALUES)
		{
			if(m.getId() != id)
				continue;
			return m;
		}
		return null;
	}
	
	public int getId()
	{
		return _id;
	}
}