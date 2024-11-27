package l2.gameserver.network.l2.s2c;

import l2.gameserver.model.Player;

public class ExSetCompassZoneCode extends L2GameServerPacket
{
	public static final int ZONE_ALTERED = 8;
	public static final int ZONE_ALTERED2 = 9;
	public static final int ZONE_REMINDER = 10;
	public static final int ZONE_SIEGE = 11;
	public static final int ZONE_PEACE = 12;
	public static final int ZONE_SSQ = 13;
	public static final int ZONE_PVP = 14;
	public static final int ZONE_GENERAL_FIELD = 15;
	public static final int ZONE_PVP_FLAG = 16384;
	public static final int ZONE_ALTERED_FLAG = 256;
	public static final int ZONE_SIEGE_FLAG = 2048;
	public static final int ZONE_PEACE_FLAG = 4096;
	public static final int ZONE_SSQ_FLAG = 8192;
	private final int _zone;
	
	public ExSetCompassZoneCode(Player player)
	{
		this(player.getZoneMask());
	}
	
	public ExSetCompassZoneCode(int zoneMask)
	{
		_zone = (zoneMask & 256) == 256 ? 8 : (zoneMask & 2048) == 2048 ? 11 : (zoneMask & 16384) == 16384 ? 14 : (zoneMask & 4096) == 4096 ? 12 : (zoneMask & 8192) == 8192 ? 13 : 15;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(50);
		writeD(_zone);
	}
}