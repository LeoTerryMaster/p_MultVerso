package l2.gameserver.network.l2.s2c;

public class GMHide extends L2GameServerPacket
{
	private final int obj_id;
	
	public GMHide(int id)
	{
		obj_id = id;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(147);
		writeD(obj_id);
	}
}