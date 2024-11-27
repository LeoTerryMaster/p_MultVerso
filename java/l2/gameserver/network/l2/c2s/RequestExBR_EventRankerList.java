package l2.gameserver.network.l2.c2s;

public class RequestExBR_EventRankerList extends L2GameClientPacket
{
	private int unk;
	private int unk2;
	private int unk3;
	
	@Override
	protected void readImpl()
	{
		unk = readD();
		unk2 = readD();
		unk3 = readD();
	}
	
	@Override
	protected void runImpl()
	{
	}
}