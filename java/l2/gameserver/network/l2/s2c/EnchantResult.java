package l2.gameserver.network.l2.s2c;

public class EnchantResult extends L2GameServerPacket
{
	public static final EnchantResult SUCESS = new EnchantResult(0, 0, 0);
	public static final EnchantResult CANCEL = new EnchantResult(2, 0, 0);
	public static final EnchantResult BLESSED_FAILED = new EnchantResult(3, 0, 0);
	public static final EnchantResult FAILED_NO_CRYSTALS = new EnchantResult(4, 0, 0);
	public static final EnchantResult ANCIENT_FAILED = new EnchantResult(5, 0, 0);
	private final int _resultId;
	private final int _crystalId;
	private final long _count;
	
	public EnchantResult(int resultId, int crystalId, long count)
	{
		_resultId = resultId;
		_crystalId = crystalId;
		_count = count;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(129);
		writeD(_resultId);
		writeD(_crystalId);
		writeQ(_count);
	}
}