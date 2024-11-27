package l2.gameserver.network.l2.s2c;

public class ExAttributeEnchantResult extends L2GameServerPacket
{
	private final int _result;
	
	public ExAttributeEnchantResult(int unknown)
	{
		_result = unknown;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeEx(97);
		writeD(_result);
	}
}