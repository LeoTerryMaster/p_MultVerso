package l2.gameserver.network.l2.c2s;

public class RequestPCCafeCouponUse extends L2GameClientPacket
{
	private String _unknown;
	
	@Override
	protected void readImpl()
	{
		_unknown = readS();
	}
	
	@Override
	protected void runImpl()
	{
	}
}