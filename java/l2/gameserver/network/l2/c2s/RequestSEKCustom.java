package l2.gameserver.network.l2.c2s;

public class RequestSEKCustom extends L2GameClientPacket
{
	private int SlotNum;
	private int Direction;
	
	@Override
	protected void readImpl()
	{
		SlotNum = readD();
		Direction = readD();
	}
	
	@Override
	protected void runImpl()
	{
	}
}