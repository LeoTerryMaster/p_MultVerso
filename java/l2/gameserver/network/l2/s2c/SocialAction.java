package l2.gameserver.network.l2.s2c;

public class SocialAction extends L2GameServerPacket
{
	public static final int GREETING = 2;
	public static final int VICTORY = 3;
	public static final int ADVANCE = 4;
	public static final int NO = 5;
	public static final int YES = 6;
	public static final int BOW = 7;
	public static final int UNAWARE = 8;
	public static final int WAITING = 9;
	public static final int LAUGH = 10;
	public static final int APPLAUD = 11;
	public static final int DANCE = 12;
	public static final int SORROW = 13;
	public static final int CHARM = 14;
	public static final int LEVEL_UP = 15;
	public static final int COUPLE_BOW = 16;
	private final int _playerId;
	private final int _actionId;
	
	public SocialAction(int playerId, int actionId)
	{
		_playerId = playerId;
		_actionId = actionId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(45);
		writeD(_playerId);
		writeD(_actionId);
	}
}