package l2.gameserver.utils;

public enum Language
{
	ENGLISH("en"),
	RUSSIAN("ru");
	
	public static final Language[] VALUES;
	
	static
	{
		VALUES = Language.values();
	}
	
	private final String _shortName;
	
	Language(String shortName)
	{
		_shortName = shortName;
	}
	
	public String getShortName()
	{
		return _shortName;
	}
}