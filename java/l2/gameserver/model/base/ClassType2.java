package l2.gameserver.model.base;

public enum ClassType2
{
	None(10280, 10612),
	Warrior(10281, 10289),
	Knight(10282, 10288),
	Rogue(10283, 10290),
	Healer(10285, 10291),
	Enchanter(10287, 10293),
	Summoner(10286, 10294),
	Wizard(10284, 10292);
	
	public static final ClassType2[] VALUES;
	
	static
	{
		VALUES = ClassType2.values();
	}
	
	private final int _certificate;
	private final int _transformation;
	
	ClassType2(int cer, int tra)
	{
		_certificate = cer;
		_transformation = tra;
	}
	
	public int getCertificateId()
	{
		return _certificate;
	}
	
	public int getTransformationId()
	{
		return _transformation;
	}
}