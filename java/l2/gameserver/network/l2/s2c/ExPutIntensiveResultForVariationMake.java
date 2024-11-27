package l2.gameserver.network.l2.s2c;

public class ExPutIntensiveResultForVariationMake extends L2GameServerPacket
{
	public static final ExPutIntensiveResultForVariationMake FAIL_PACKET = new ExPutIntensiveResultForVariationMake(0, 0, 0, 0, false);
	private final int _refinerItemObjId;
	private final int _lifestoneItemId;
	private final int _gemstoneItemId;
	private final int _result;
	private final long _gemstoneCount;
	
	public ExPutIntensiveResultForVariationMake(int refinerItemObjId, int lifeStoneId, int gemstoneItemId, long gemstoneCount, boolean isSuccess)
	{
		_refinerItemObjId = refinerItemObjId;
		_lifestoneItemId = lifeStoneId;
		_gemstoneItemId = gemstoneItemId;
		_gemstoneCount = gemstoneCount;
		_result = isSuccess ? 1 : 0;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(83);
		writeD(_refinerItemObjId);
		writeD(_lifestoneItemId);
		writeD(_gemstoneItemId);
		writeD((int) _gemstoneCount);
		writeD(_result);
	}
}