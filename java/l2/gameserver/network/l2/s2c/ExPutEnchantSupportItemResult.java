package l2.gameserver.network.l2.s2c;

public class ExPutEnchantSupportItemResult extends L2GameServerPacket
{
	public static final L2GameServerPacket FAIL = new ExPutEnchantSupportItemResult(0);
	public static final L2GameServerPacket SUCCESS = new ExPutEnchantSupportItemResult(1);
	private final int _result;
	
	private ExPutEnchantSupportItemResult(int result)
	{
		_result = result;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(130);
		writeD(_result);
	}
}