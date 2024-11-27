package l2.gameserver.network.l2.s2c;

public class ExBlockUpSetList extends L2GameServerPacket
{
	private final int BlockUpType = 0;
	
	@Override
	protected void writeImpl()
	{
		writeEx(151);
		writeD(BlockUpType);
		switch(BlockUpType)
		{
			case 0:
			{
				break;
			}
			case 1:
			{
				break;
			}
			case 2:
			{
				break;
			}
			case 3:
			{
				break;
			}
			case 4:
			{
				break;
			}
			case 5:
			{
				break;
			}
		}
	}
}