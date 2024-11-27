package l2.gameserver.network.l2.s2c;

public class ExPutCommissionResultForVariationMake extends L2GameServerPacket
{
	public static final ExPutCommissionResultForVariationMake FAIL_PACKET = new ExPutCommissionResultForVariationMake();
	private final int _gemstoneObjId;
	private final int _serverId;
	private final int _result;
	private final long _gemstoneCount;
	private final long _requiredGemstoneCount;
	
	private ExPutCommissionResultForVariationMake()
	{
		_gemstoneObjId = 0;
		_serverId = 1;
		_gemstoneCount = 0;
		_requiredGemstoneCount = 0;
		_result = 0;
	}
	
	public ExPutCommissionResultForVariationMake(int gemstoneObjId, long count)
	{
		_gemstoneObjId = gemstoneObjId;
		_serverId = 1;
		_gemstoneCount = count;
		_requiredGemstoneCount = count;
		_result = 1;
	}
	
	@Override
	protected void writeImpl()
	{
		writeEx(84);
		writeD(_gemstoneObjId);
		writeD(_serverId);
		writeD((int) _gemstoneCount);
		writeD((int) _requiredGemstoneCount);
		writeD(_result);
	}
}