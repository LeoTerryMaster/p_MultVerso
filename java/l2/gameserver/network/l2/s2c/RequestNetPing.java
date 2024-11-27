package l2.gameserver.network.l2.s2c;

public class RequestNetPing extends L2GameServerPacket
{
	private final int timestamp;
	
	public RequestNetPing(int timestamp)
	{
		this.timestamp = timestamp;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(211);
		writeD(timestamp);
	}
}