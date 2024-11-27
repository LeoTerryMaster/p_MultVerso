package l2.gameserver.network.l2.s2c;

public class ExPVPMatchCCRetire extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC = new ExPVPMatchCCRetire();
	
	@Override
	public void writeImpl()
	{
		writeEx(139);
	}
}