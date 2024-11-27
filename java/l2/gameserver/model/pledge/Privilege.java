package l2.gameserver.model.pledge;

public enum Privilege
{
	FREE,
	CL_JOIN_CLAN,
	CL_GIVE_TITLE,
	CL_VIEW_WAREHOUSE,
	CL_MANAGE_RANKS,
	CL_PLEDGE_WAR,
	CL_DISMISS,
	CL_REGISTER_CREST,
	CL_APPRENTICE,
	CL_TROOPS_FAME,
	CH_ENTER_EXIT,
	CH_USE_FUNCTIONS,
	CH_AUCTION,
	CH_DISMISS,
	CH_SET_FUNCTIONS,
	CS_FS_ENTER_EXIT,
	CS_FS_MANOR_ADMIN,
	CS_FS_SIEGE_WAR,
	CS_FS_USE_FUNCTIONS,
	CS_FS_DISMISS,
	CS_FS_MANAGER_TAXES,
	CS_FS_MERCENARIES,
	CS_FS_SET_FUNCTIONS;
	
	public static final int ALL = 8388606;
	public static final int NONE = 0;
	private final int _mask;
	
	Privilege()
	{
		_mask = ordinal() == 0 ? 0 : 1 << ordinal();
	}
	
	public int mask()
	{
		return _mask;
	}
}