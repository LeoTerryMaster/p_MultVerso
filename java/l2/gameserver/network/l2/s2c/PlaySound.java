package l2.gameserver.network.l2.s2c;

import l2.gameserver.utils.Location;

public class PlaySound extends L2GameServerPacket
{
	public static final L2GameServerPacket SIEGE_VICTORY = new PlaySound("Siege_Victory");
	public static final L2GameServerPacket B04_S01 = new PlaySound("B04_S01");
	public static final L2GameServerPacket HB01 = new PlaySound(Type.MUSIC, "HB01", 0, 0, 0, 0, 0);
	private final Type _type;
	private final String _soundFile;
	private final int _hasCenterObject;
	private final int _objectId;
	private final int _x;
	private final int _y;
	private final int _z;
	
	public PlaySound(String soundFile)
	{
		this(Type.SOUND, soundFile, 0, 0, 0, 0, 0);
	}
	
	public PlaySound(Type type, String soundFile, int c, int objectId, Location loc)
	{
		this(type, soundFile, c, objectId, loc == null ? 0 : loc.x, loc == null ? 0 : loc.y, loc == null ? 0 : loc.z);
	}
	
	public PlaySound(Type type, String soundFile, int c, int objectId, int x, int y, int z)
	{
		_type = type;
		_soundFile = soundFile;
		_hasCenterObject = c;
		_objectId = objectId;
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(152);
		writeD(_type.ordinal());
		writeS(_soundFile);
		writeD(_hasCenterObject);
		writeD(_objectId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
	
	public enum Type
	{
		SOUND,
		MUSIC,
		VOICE;
	}
}