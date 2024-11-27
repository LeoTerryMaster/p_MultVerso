package l2.gameserver.network.l2.s2c;

public class ExBlockUpSetState extends L2GameServerPacket
{
	private final int BlockUpStateType = 0;
	
	@Override
	protected void writeImpl()
	{
		writeEx(152);
		writeD(BlockUpStateType);
		switch(BlockUpStateType)
		{
			case 0:
			{
				break;
			}
			case 1:
			{
				break;
			}
		}
	}
}