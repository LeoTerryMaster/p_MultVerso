package l2.gameserver.network.l2.s2c;

public class ExPutEnchantTargetItemResult extends L2GameServerPacket
{
	public static final L2GameServerPacket FAIL = new ExPutEnchantTargetItemResult(0);
	public static final L2GameServerPacket SUCCESS = new ExPutEnchantTargetItemResult(1);
	private final int _result;
	
	private ExPutEnchantTargetItemResult(int result)
	{
		_result = result;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(129);
		writeD(_result);
	}
}