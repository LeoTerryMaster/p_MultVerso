package l2.gameserver.model.entity.oly;

public enum CompetitionType
{
	TEAM_CLASS_FREE(0),
	CLASS_FREE(1),
	CLASS_INDIVIDUAL(2);
	
	private final int _type_idx;
	
	CompetitionType(int type_idx)
	{
		_type_idx = type_idx;
	}
	
	public static CompetitionType getTypeOf(int idx)
	{
		for(CompetitionType type : CompetitionType.values())
		{
			if(type.getTypeIdx() != idx)
				continue;
			return type;
		}
		return null;
	}
	
	public int getTypeIdx()
	{
		return _type_idx;
	}
}