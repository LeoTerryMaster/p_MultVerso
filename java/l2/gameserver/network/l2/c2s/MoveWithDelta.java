package l2.gameserver.network.l2.c2s;

public class MoveWithDelta extends L2GameClientPacket
{
	private int _dx;
	private int _dy;
	private int _dz;
	
	@Override
	protected void readImpl()
	{
		_dx = readD();
		_dy = readD();
		_dz = readD();
	}
	
	@Override
	protected void runImpl()
	{
	}
}