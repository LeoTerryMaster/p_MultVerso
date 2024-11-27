package l2.gameserver.network.l2.s2c;

public class ShowPCCafeCouponShowUI extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeEx(67);
	}
}