package l2.gameserver.network.l2.s2c;

public class ExPutItemResultForVariationMake extends L2GameServerPacket
{
	public static final ExPutItemResultForVariationMake FAIL_PACKET = new ExPutItemResultForVariationMake(0, false);
	private final int _itemObjId;
	private final int _serverId;
	private final int _result;
	
	public ExPutItemResultForVariationMake(int itemObjId, boolean isSuccess)
	{
		_itemObjId = itemObjId;
		_serverId = 0;
		_result = isSuccess ? 1 : 0;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(82);
		writeD(_itemObjId);
		writeD(_serverId);
		writeD(_result);
	}
}