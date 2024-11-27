package l2.gameserver.model.entity.residence;

public enum ResidenceType
{
	Castle,
	ClanHall,
	Fortress,
	Dominion;
	
	public static final ResidenceType[] VALUES;
	
	static
	{
		VALUES = ResidenceType.values();
	}
}