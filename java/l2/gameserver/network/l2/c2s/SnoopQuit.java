package l2.gameserver.network.l2.c2s;

public class SnoopQuit extends L2GameClientPacket
{
	private int _snoopID;
	
	@Override
	protected void readImpl()
	{
		_snoopID = readD();
	}
	
	@Override
	protected void runImpl()
	{
	}
}