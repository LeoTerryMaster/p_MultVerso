package l2.gameserver.templates.item;

public enum Bodypart
{
	NONE(0),
	CHEST(1024),
	BELT(268435456),
	RIGHT_BRACELET(1048576),
	LEFT_BRACELET(2097152),
	FULL_ARMOR(32768),
	HEAD(64),
	HAIR(65536),
	FACE(262144),
	HAIR_ALL(524288),
	UNDERWEAR(1),
	BACK(8192),
	NECKLACE(8),
	LEGS(2048),
	FEET(4096),
	GLOVES(512),
	RIGHT_HAND(128),
	LEFT_HAND(256),
	LEFT_RIGHT_HAND(16384),
	RIGHT_EAR(2),
	LEFT_EAR(4),
	RIGHT_FINGER(16),
	FORMAL_WEAR(131072),
	TALISMAN(4194304),
	LEFT_FINGER(32),
	WOLF(-100, CHEST),
	GREAT_WOLF(-104, CHEST),
	HATCHLING(-101, CHEST),
	STRIDER(-102, CHEST),
	BABY_PET(-103, CHEST),
	PENDANT(-105, NECKLACE);
	
	private final int _mask;
	private final Bodypart _real;
	
	Bodypart(int mask)
	{
		this(mask, null);
	}
	
	Bodypart(int mask, Bodypart real)
	{
		_mask = mask;
		_real = real;
	}
	
	public int mask()
	{
		return _mask;
	}
	
	public Bodypart getReal()
	{
		return _real;
	}
}