package l2.gameserver.network.l2.s2c;

public class ExSearchOrc extends L2GameServerPacket
{
	private static final byte[] _test = {-28, -85, -114, -59, -23, -7, -122, 123, -98, 93, -125, 20, 5, -44, 72, 1, -51, -94, -115, -112, 98, -116, -38, 50, 123, 27, -121, 109, 8, -60, -31, 86, -101, 59, -61, 64, -33, -24, -41, -31, -104, 56, 28, -91, -114, 69, 63, -14, 94, 28, 89, -114, 116, 1, -98, -62, 0, -107, -80, 29, -121, -19, -100, -118};
	
	@Override
	protected final void writeImpl()
	{
		writeEx(69);
		writeB(_test);
	}
}